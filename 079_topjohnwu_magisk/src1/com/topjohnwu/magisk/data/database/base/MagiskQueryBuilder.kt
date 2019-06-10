package com.topjohnwu.magisk.data.database.base

import androidx.annotation.StringDef
import com.topjohnwu.magisk.data.database.base.Order.Companion.ASC
import com.topjohnwu.magisk.data.database.base.Order.Companion.DESC

interface MagiskQueryBuilder {

    val requestType: String
    var table: String

    companion object {
        inline operator fun <reified Builder : MagiskQueryBuilder> invoke(builder: Builder.() -> Unit): MagiskQuery =
            Builder::class.java.newInstance()
                .apply(builder)
                .toString()
                .let {
                    MagiskQuery(it)
                }
    }

}

class Delete : MagiskQueryBuilder {
    override val requestType: String = "DELETE FROM"
    override var table = ""

    private var condition = ""

    fun condition(builder: Condition.() -> Unit) {
        condition = Condition().apply(builder).toString()
    }

    override fun toString(): String {
        return listOf(requestType, table, condition).joinToString(" ")
    }
}

class Select : MagiskQueryBuilder {
    override val requestType: String get() = "SELECT $fields FROM"
    override lateinit var table: String

    private var fields = "*"
    private var condition = ""
    private var orderField = ""

    fun fields(vararg newFields: String) {
        if (newFields.isEmpty()) {
            fields = "*"
            return
        }
        fields = newFields.joinToString(", ")
    }

    fun condition(builder: Condition.() -> Unit) {
        condition = Condition().apply(builder).toString()
    }

    fun orderBy(field: String, @OrderStrict order: String) {
        orderField = "ORDER BY $field $order"
    }

    override fun toString(): String {
        return listOf(requestType, table, condition, orderField).joinToString(" ")
    }
}

class Replace : Insert() {
    override val requestType: String = "REPLACE INTO"
}

open class Insert : MagiskQueryBuilder {
    override val requestType: String = "INSERT INTO"
    override lateinit var table: String

    private val keys get() = _values.keys.joinToString(",")
    private val values get() = _values.values.joinToString(",") {
        when (it) {
            is Boolean -> if (it) "1" else "0"
            is Number -> it.toString()
            else -> "\"$it\""
        }
    }
    private var _values: Map<String, Any> = mapOf()

    fun values(vararg pairs: Pair<String, Any>) {
        _values = pairs.toMap()
    }

    fun values(values: Map<String, Any>) {
        _values = values
    }

    override fun toString(): String {
        return listOf(requestType, table, "($keys) VALUES($values)").joinToString(" ")
    }
}

class Condition {

    private val conditionWord = "WHERE %s"
    private var condition: String = ""

    fun equals(field: String, value: Any) {
        condition = when (value) {
            is String -> "$field=\"$value\""
            else -> "$field=$value"
        }
    }

    fun greaterThan(field: String, value: String) {
        condition = "$field > $value"
    }

    fun lessThan(field: String, value: String) {
        condition = "$field < $value"
    }

    fun greaterOrEqualTo(field: String, value: String) {
        condition = "$field >= $value"
    }

    fun lessOrEqualTo(field: String, value: String) {
        condition = "$field <= $value"
    }

    fun and(builder: Condition.() -> Unit) {
        condition = "($condition AND ${Condition().apply(builder).condition})"
    }

    fun or(builder: Condition.() -> Unit) {
        condition = "($condition OR ${Condition().apply(builder).condition})"
    }

    override fun toString(): String {
        return conditionWord.format(condition)
    }
}

class Order {

    @set:OrderStrict
    var order = DESC
    var field = ""

    companion object {
        const val ASC = "ASC"
        const val DESC = "DESC"
    }

}

@StringDef(ASC, DESC)
@Retention(AnnotationRetention.SOURCE)
annotation class OrderStrict