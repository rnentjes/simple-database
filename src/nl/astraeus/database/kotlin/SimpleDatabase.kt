package nl.astraeus.database.kotlin

import nl.astraeus.database.Persister
import java.sql.ResultSet

/**
 * Created by rnentjes on 30-9-15.
 */

fun transaction(block: () -> Unit) {
    try {
        Persister.begin()

        block()

        Persister.commit()
    } finally {
        if (Persister.transactionActive()) {
            Persister.rollback()
        }
    }
}

fun insert(obj: Any) = Persister.insert(obj)
fun update(obj: Any) = Persister.update(obj)
fun delete(obj: Any) = Persister.delete(obj)

fun executeUpdate(sql: String, params: Array<String>) : Int = Persister.executeUpdate(sql, params)
fun executeQuery(sql: String, params: Array<String>) : ResultSet = Persister.executeQuery(sql, params)

