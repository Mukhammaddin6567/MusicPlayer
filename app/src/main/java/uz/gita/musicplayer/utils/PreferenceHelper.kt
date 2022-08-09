package uz.gita.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SharedPreference(context: Context, preferences: SharedPreferences? = null) {
    private val preferences =
        preferences ?: context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    inner class BooleanPreference(private val defValue: Boolean = false) :
        ReadWriteProperty<SharedPreference, Boolean> {
        override fun getValue(thisRef: SharedPreference, property: KProperty<*>) =
            preferences.getBoolean(property.name, defValue)

        override fun setValue(thisRef: SharedPreference, property: KProperty<*>, value: Boolean) =
            preferences.edit { putBoolean(property.name, value).apply() }
    }

    inner class IntPreference(private val defValue: Int = 0) : ReadWriteProperty<Any, Int> {
        override fun getValue(thisRef: Any, property: KProperty<*>) =
            preferences.getInt(property.name, defValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) =
            preferences.edit { putInt(property.name, value).apply() }
    }

    inner class LongPreference(private val defValue: Long = 0L) : ReadWriteProperty<Any, Long> {
        override fun getValue(thisRef: Any, property: KProperty<*>) =
            preferences.getLong(property.name, defValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) =
            preferences.edit { putLong(property.name, value).apply() }
    }

    inner class StringPreference(private val defValue: String? = null) :
        ReadWriteProperty<Any, String> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String =
            preferences.getString(property.name, defValue) ?: ""

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) =
            preferences.edit { putString(property.name, value).apply() }

    }

}
