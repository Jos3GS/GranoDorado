package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.domain.model.Empleado
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

object FirestoreService {
    val firestore get() = try { Firebase.firestore } catch (_: Exception) { null }

    fun observarEmpleados(): Flow<List<Empleado>> {
        val fs = firestore ?: return emptyFlow()
        return try {
            fs.collection("empleados").snapshots.map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data<Empleado>()
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (_: Exception) {
            emptyFlow()
        }
    }

    suspend fun obtenerEmpleados(): List<Empleado> {
        val fs = firestore ?: return emptyList()
        return try {
            val snapshot = fs.collection("empleados").get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<Empleado>()
                } catch (e: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun guardarEmpleado(empleado: Empleado): Result<Unit> {
        val fs = firestore ?: return Result.failure(Exception("Firestore no disponible"))
        return try {
            fs.collection("empleados").document(empleado.id).set(empleado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inicializarSiEstaVacio(): Boolean {
        val fs = firestore ?: return false
        return try {
            val snapshot = fs.collection("empleados").get()
            if (snapshot.documents.isEmpty()) {
                val iniciales = MockDataProviders.inicialEmpleados()
                for (emp in iniciales) {
                    fs.collection("empleados").document(emp.id).set(emp)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
