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

interface GsonTypeDecoder {
    fun typeEncoding(): String
    fun getClass(serializedType: String): Class<*>
    fun getEncodedName(classIn: Class<*>): String
}

class BasicGsonTypeDecoder private constructor(
    private val encodingName: String,
    private val encodingMap: Map<String, Class<*>>
) : GsonTypeDecoder {

    private val classMap = encodingMap.entries.associateBy({ it.value }) { it.key }

    override fun typeEncoding(): String {
        return encodingName
    }

    override fun getClass(serializedType: String): Class<*> {
        if (encodingMap.containsKey(serializedType)) {
            return encodingMap.getValue(serializedType)
        } else {
            throw IllegalStateException(
                "Gson Mapping does not exist for type $serializedType, " +
                        "please make sure you registered a mapping for this type in your GsonTypeDecoder!"
            )
        }
    }

    override fun getEncodedName(classIn: Class<*>): String {
        if (classMap.containsKey(classIn)) {
            return classMap.getValue(classIn)
        } else {
            throw IllegalStateException(
                "Gson mapping does not exist for class ${classIn.simpleName}, " +
                        "please make sure you registered a mapping for this class in your GsonTypeDecoder!"
            )
        }
    }

    class Builder(private val encodingName: String) {
        private val encodingMap = mutableMapOf<String, Class<*>>()

        fun addMapping(encoding: String, classIn: Class<*>) = apply { this.encodingMap[encoding] = classIn }

        fun build(): BasicGsonTypeDecoder {
            return BasicGsonTypeDecoder(encodingName, encodingMap)
        }
    }
}
