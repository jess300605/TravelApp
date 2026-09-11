package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.R
import com.example.model.Destination
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class DestinationRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("destinations_cache", Context.MODE_PRIVATE)

    private val _destinations = MutableStateFlow<List<Destination>>(emptyList())
    val destinations: StateFlow<List<Destination>> = _destinations.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var snapshotListener: ListenerRegistration? = null

    init {
        // First load from local cache or seed samples
        loadFromCache()

        // Then check and connect Firestore if available
        initFirestore()
    }

    private fun initFirestore() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                listenToFirestore()
            }
        } catch (e: Exception) {
            firestore = null
        }
    }

    private fun listenToFirestore() {
        val db = firestore ?: return
        try {
            snapshotListener?.remove()
            snapshotListener = db.collection("destinations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }

                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data ->
                            Destination.fromMap(doc.id, data)
                        }
                    }

                    if (list.isNotEmpty()) {
                        _destinations.value = list.sortedByDescending { it.createdAt }
                        saveToCache(_destinations.value)
                    } else if (_destinations.value.isEmpty()) {
                        // If firestore is completely empty, upload initial samples
                        CoroutineScope(Dispatchers.IO).launch {
                            seedDefaultDestinations()
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFromCache() {
        val json = prefs.getString("destinations_json", null)
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<Destination>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Destination(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            country = obj.getString("country"),
                            price = obj.getDouble("price"),
                            description = obj.getString("description"),
                            imageUri = obj.getString("imageUri"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                _destinations.value = list.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                seedInitialLocally()
            }
        } else {
            seedInitialLocally()
        }
    }

    private fun seedInitialLocally() {
        val cancunUri = "android.resource://${context.packageName}/${R.drawable.travel_cancun}"
        val parisUri = "android.resource://${context.packageName}/${R.drawable.travel_paris}"
        val tokyoUri = "android.resource://${context.packageName}/${R.drawable.travel_tokyo}"

        val initial = listOf(
            Destination(
                id = "dest_cancun_01",
                name = "Cancún All-Inclusive Resort",
                country = "México",
                price = 599.00,
                description = "Disfruta de playas caribeñas de arena blanca, buffet internacional y deportes acuáticos en Riviera Maya.",
                imageUri = cancunUri,
                createdAt = System.currentTimeMillis() - 30000
            ),
            Destination(
                id = "dest_paris_02",
                name = "París Clásico y Museo del Louvre",
                country = "Francia",
                price = 1250.00,
                description = "Paquete completo con paseos en barco por el río Sena, ascenso a la Torre Eiffel y guía en español.",
                imageUri = parisUri,
                createdAt = System.currentTimeMillis() - 20000
            ),
            Destination(
                id = "dest_tokyo_03",
                name = "Tokio y Monte Fuji Tradicional",
                country = "Japón",
                price = 1850.00,
                description = "Explora los templos históricos de Kioto, la modernidad de Shibuya y las vistas nevadas del Monte Fuji.",
                imageUri = tokyoUri,
                createdAt = System.currentTimeMillis() - 10000
            )
        )
        _destinations.value = initial
        saveToCache(initial)
    }

    private suspend fun seedDefaultDestinations() {
        val db = firestore ?: return
        for (item in _destinations.value) {
            try {
                db.collection("destinations").document(item.id).set(item.toMap()).await()
            } catch (_: Exception) {}
        }
    }

    private fun saveToCache(list: List<Destination>) {
        try {
            val array = JSONArray()
            for (dest in list) {
                val obj = JSONObject().apply {
                    put("id", dest.id)
                    put("name", dest.name)
                    put("country", dest.country)
                    put("price", dest.price)
                    put("description", dest.description)
                    put("imageUri", dest.imageUri)
                    put("createdAt", dest.createdAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("destinations_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createDestination(destination: Destination): Result<Unit> {
        _isSyncing.value = true
        return try {
            // Update local state immediately for snappy UX
            val updated = listOf(destination) + _destinations.value
            _destinations.value = updated
            saveToCache(updated)

            // Sync with Firestore if available
            val db = firestore
            if (db != null) {
                try {
                    db.collection("destinations")
                        .document(destination.id)
                        .set(destination.toMap())
                        .await()
                } catch (e: Exception) {
                    // Log or handle remote sync error
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun updateDestination(destination: Destination): Result<Unit> {
        _isSyncing.value = true
        return try {
            // Update local state
            val updated = _destinations.value.map {
                if (it.id == destination.id) destination else it
            }
            _destinations.value = updated
            saveToCache(updated)

            // Sync with Firestore if available
            val db = firestore
            if (db != null) {
                try {
                    db.collection("destinations")
                        .document(destination.id)
                        .set(destination.toMap(), SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    // Remote sync error
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun deleteDestination(destinationId: String): Result<Unit> {
        _isSyncing.value = true
        return try {
            // Update local state
            val updated = _destinations.value.filter { it.id != destinationId }
            _destinations.value = updated
            saveToCache(updated)

            // Delete in Firestore
            val db = firestore
            if (db != null) {
                try {
                    db.collection("destinations")
                        .document(destinationId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    // Remote sync error
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    fun getDestinationById(id: String): Destination? {
        return _destinations.value.find { it.id == id }
    }
}
