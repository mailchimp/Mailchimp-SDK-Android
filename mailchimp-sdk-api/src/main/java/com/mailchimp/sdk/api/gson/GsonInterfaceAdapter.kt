/*
 * Licensed under the Mailchimp Mobile SDK License Agreement (the "License");
 * you may not use this file except in compliance with the License. Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either or express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mailchimp.sdk.api.gson

import com.google.gson.*
import java.lang.reflect.Type

/**
 * This class can be registered with GSON to automatically serialize and deserialize multiple implementations of an interface.
 *
 * @param typeDecoder: the provided type decoder is used to know what to encode the implementation of an interface as in
 * the json object.
 */
class GsonInterfaceAdapter<T : Any>(private val typeDecoder: GsonTypeDecoder) : JsonDeserializer<T>, JsonSerializer<T> {

    /**
     * Serializes the object and then adds in an addition field that encodes the specific implementation of the interface.
     * Uses the typeDecoder to know what to encode the type as and what the name of the field should be.
     */
    override fun serialize(elem: T, type: Type, jsonSerializationContext: JsonSerializationContext): JsonElement {
        val actualType = typeForName(elem::class.java.name)
        val jsonObject = jsonSerializationContext.serialize(elem, actualType).asJsonObject
        val typeEncoding = typeDecoder.getEncodedName(elem::class.java)
        jsonObject.addProperty(typeDecoder.typeEncoding(), typeEncoding)
        return jsonObject
    }

    /**
     * Deserializes the object using the specific implementation specified in the json object
     */
    @Throws(JsonParseException::class)
    override fun deserialize(elem: JsonElement, interfaceType: Type, context: JsonDeserializationContext): T {
        val wrapper = elem as JsonObject
        val typeEncoding = typeDecoder.typeEncoding()

        if (!wrapper.has(typeEncoding)) {
            throw IllegalStateException(
                "Json object $wrapper does not contain expected $typeEncoding property. Without " +
                    "the $typeEncoding property, it is unknown how to deserialize this object."
            )
        }

        val serializedType = wrapper.get(typeEncoding).asString
        val typeName = typeDecoder.getClass(serializedType).name
        val actualType = typeForName(typeName)
        return context.deserialize(wrapper, actualType)
    }

    private fun typeForName(className: String): Type {
        try {
            return Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e)
        }
    }
}