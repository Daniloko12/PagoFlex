package com.example.pagoflex

import android.content.SharedPreferences

class PreferenciasEnMemoria : SharedPreferences {
    private val valores = mutableMapOf<String, Any>()

    private fun valor(key: String?): Any? = key?.let { valores[it] }

    override fun getAll(): MutableMap<String, *> = valores.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? = valor(key) as? String ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        valor(key) as? MutableSet<String> ?: defValues

    override fun getInt(key: String?, defValue: Int): Int = valor(key) as? Int ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = valor(key) as? Long ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = valor(key) as? Float ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = valor(key) as? Boolean ?: defValue

    override fun contains(key: String?): Boolean = valor(key) != null

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class Editor : SharedPreferences.Editor {
        private val cambios = mutableMapOf<String, Any?>()
        private var borrarTodo = false

        private fun poner(key: String?, valor: Any?): SharedPreferences.Editor {
            if (key != null) cambios[key] = valor
            return this
        }

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = poner(key, value)

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = poner(key, values)

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = poner(key, value)

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = poner(key, value)

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = poner(key, value)

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = poner(key, value)

        override fun remove(key: String?): SharedPreferences.Editor = poner(key, null)

        override fun clear(): SharedPreferences.Editor {
            borrarTodo = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (borrarTodo) valores.clear()
            cambios.forEach { (clave, valor) -> if (valor == null) valores.remove(clave) else valores[clave] = valor }
        }
    }
}
