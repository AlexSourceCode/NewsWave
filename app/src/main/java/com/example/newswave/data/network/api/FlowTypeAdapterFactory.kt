package com.example.newswave.data.network.api

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.lang.reflect.ParameterizedType

class FlowTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (rawType != Flow::class.java) {
            return null
        }
        val elementType = (type.type as ParameterizedType).actualTypeArguments[0]
        val elementAdapter = gson.getAdapter(TypeToken.get(elementType))
        return FlowTypeAdapter(elementAdapter) as TypeAdapter<T>
    }
}

class FlowTypeAdapter<T>(private val elementAdapter: TypeAdapter<T>) : TypeAdapter<Flow<T>>() {
    override fun write(out: JsonWriter, value: Flow<T>?) {
        if (value == null) {
            out.nullValue()
            return
        }
        val list = runBlocking { value.toList() }
        out.beginArray()
        for (element in list) {
            elementAdapter.write(out, element)
        }
        out.endArray()
    }

    override fun read(`in`: JsonReader): Flow<T> {
        val list = mutableListOf<T>()
        if (`in`.peek() == JsonToken.BEGIN_ARRAY) {
            `in`.beginArray()
            while (`in`.hasNext()) {
                list.add(elementAdapter.read(`in`))
            }
            `in`.endArray()
        } else if (`in`.peek() == JsonToken.BEGIN_OBJECT) {
            list.add(elementAdapter.read(`in`))
        }
        return flow {
            list.forEach { emit(it) }
        }
    }
}
