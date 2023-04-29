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

import com.google.gson.*
import com.mailchimp.sdk.api.gson.BasicGsonTypeDecoder
import com.mailchimp.sdk.api.gson.GsonInterfaceAdapter
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.MergeFieldValue
import com.mailchimp.sdk.api.model.mergefields.StringMergeFieldValue
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Type

class InterfaceAdapterTest {

    @Test
    fun testDeserializeStringMergeField() {
        val adapter = GsonInterfaceAdapter<MergeFieldValue>(standardTypeDecoder())

        val json = JsonObject()
        json.addProperty("type", "string")
        json.addProperty("value", "MyTestString")

        val deserializedObject = adapter.deserialize(json, MergeFieldValue::class.java, GsonContextImpl())
        assertNotNull(deserializedObject)
        assertEquals("MyTestString", (deserializedObject as StringMergeFieldValue).value)
    }

    @Test
    fun testDeserializeAddressMergeField() {
        val adapter = GsonInterfaceAdapter<MergeFieldValue>(standardTypeDecoder())

        val json = JsonObject()
        json.addProperty("type", "address")
        json.addProperty("addr1", "675 Ponce De Leon")
        json.addProperty("addr2", "C/O Freddie Chimpster")
        json.addProperty("city", "Atlanta")
        json.addProperty("state", "GA")
        json.addProperty("country", "USA")
        json.addProperty("zip", "30308")

        val deserializedObject = adapter.deserialize(json, MergeFieldValue::class.java, GsonContextImpl())
        assertNotNull(deserializedObject)
        val address = deserializedObject as Address
        assertEquals("675 Ponce De Leon", address.addressLineOne)
        assertEquals("C/O Freddie Chimpster", address.addressLineTwo)
        assertEquals("Atlanta", address.city)
        assertEquals("GA", address.state)
        assertEquals("30308", address.zip)
    }

    @Test
    fun testDeserializeUnknownType() {
        val adapter = GsonInterfaceAdapter<MergeFieldValue>(standardTypeDecoder())

        val json = JsonObject()
        json.addProperty("type", "Jabroni")
        json.addProperty("value", "MyTestString")

        try {
            adapter.deserialize(json, MergeFieldValue::class.java, GsonContextImpl())
            fail("expected a exception to be thrown.")
        } catch (e: IllegalStateException) {
            assertNotNull(e)
            assertNotNull(e.message)
        }
    }

    @Test
    fun testDeserializeWithNoTypeValueInJson() {
        val adapter = GsonInterfaceAdapter<MergeFieldValue>(standardTypeDecoder())

        val json = JsonObject()
        json.addProperty("value", "MyTestString")

        try {
            adapter.deserialize(json, MergeFieldValue::class.java, GsonContextImpl())
            fail("expected a exception to be thrown.")
        } catch (e: IllegalStateException) {
            assertNotNull(e)
            assertNotNull(e.message)
        }
    }

    @Test
    fun testSerialize() {
        val adapter = GsonInterfaceAdapter<MergeFieldValue>(standardTypeDecoder())
        val mergeField = StringMergeFieldValue("Jabroni")

        val serialized =
            adapter.serialize(mergeField, StringMergeFieldValue::class.java, GsonContextImpl()) as JsonObject
        assertEquals("Jabroni", serialized.get("value").asString)
        assertEquals("string", serialized.get("type").asString)
    }

    @Test
    fun testsSerializeWhenMappingDoesNotExist() {
        val typeDecoder =
            BasicGsonTypeDecoder.Builder("type")
                .addMapping("address", Address::class.java)
                .build()

        val adapter = GsonInterfaceAdapter<MergeFieldValue>(typeDecoder)
        val mergeField = StringMergeFieldValue("Jabroni")

        try {
            val serialized =
                adapter.serialize(mergeField, StringMergeFieldValue::class.java, GsonContextImpl()) as JsonObject
            fail("expected a exception to be thrown.")
        } catch (e: IllegalStateException) {
            assertNotNull(e)
            assertNotNull(e.message)
        }
    }

    private fun standardTypeDecoder(): BasicGsonTypeDecoder {
        return BasicGsonTypeDecoder.Builder("type")
            .addMapping("string", StringMergeFieldValue::class.java)
            .addMapping("address", Address::class.java)
            .build()
    }

    private inner class GsonContextImpl : JsonSerializationContext, JsonDeserializationContext {
        private val gson: Gson = Gson()

        override fun serialize(src: Any): JsonElement {
            return gson.toJsonTree(src)
        }

        override fun serialize(src: Any, typeOfSrc: Type): JsonElement {
            return gson.toJsonTree(src, typeOfSrc)
        }

        @Throws(JsonParseException::class)
        override fun <R> deserialize(json: JsonElement, typeOfT: Type): R? {
            return gson.fromJson(json, typeOfT)
        }
    }
}