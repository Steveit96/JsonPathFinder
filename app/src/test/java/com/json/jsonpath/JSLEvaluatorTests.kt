package com.json.jsonpath

import android.os.Build
import com.json.jsonpath.jsl.JSL
import com.json.jsonpath.jsl.JSLMapper
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pt.rocket.utils.parser.JSLError

@RunWith(RobolectricTestRunner::class)
class JSLEvaluatorTests {

    companion object {
        private const val SAMPLE_JSON_STRING_ONE = "{\n" +
            "  \"boolean_key\": \"--- true\\n\",\n" +
            "  \"empty_string_translation\": \"\",\n" +
            "  \"key_with_description\": \"Check it out! This key has a description! (At least in some formats)\",\n" +
            "  \"key_with_line-break\": \"This translations contains\\na line-break.\",\n" +
            "  \"nested\": {\n" +
            "    \"deeply\": {\n" +
            "      \"key\": \"Wow, this key is nested even deeper.\"\n" +
            "    },\n" +
            "    \"key\": \"This key is nested inside a namespace.\"\n" +
            "  },\n" +
            "  \"null_translation\": null,\n" +
            "  \"pluralized_key\": {\n" +
            "    \"one\": \"Only one pluralization found.\",\n" +
            "    \"other\": \"Wow, you have %s pluralizations!\",\n" +
            "    \"zero\": \"You have no pluralization.\"\n" +
            "  },\n" +
            "  \"sample_collection\": [\n" +
            "    \"first item\",\n" +
            "    \"second item\",\n" +
            "    \"third item\"\n" +
            "  ],\n" +
            "  \"simple_key\": \"Just a simple key with a simple message.\",\n" +
            "  \"unverified_key\": \"This translation is not yet verified and waits for it. (In some formats we also export this status)\"\n" +
            "}"

        private const val SAMPLE_JSON_STRING_TWO = "{\n" +
            "  \"name\":\"John\",\n" +
            "  \"age\":30,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " }"
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testJsonMapper() {
        assertEquals("b", JSLMapper.select(".a", JSONObject("{\"a\": \"b\"}")))
        assertEquals("c", JSLMapper.select(".a.b", JSONObject("{\"a\": { \"b\": \"c\" }}")))
        assertEquals(2.0, JSLMapper.select(".a.b", JSONObject("{\"a\": { \"b\":  2.0 }}")))
        assertEquals(JSONArray(arrayListOf("c", "d")), JSLMapper.select(".a.b", JSONObject("{\"a\": { \"b\": [\"c\", \"d\"]}}")))
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testSampleJsonMapper() {
        assertEquals("Wow, this key is nested even deeper.", JSLMapper.select(".nested.deeply.key", JSONObject(
            SAMPLE_JSON_STRING_ONE
        )))
        assertEquals("Only one pluralization found.", JSLMapper.select(".pluralized_key.one", JSONObject(
            SAMPLE_JSON_STRING_ONE
        )))

        val expectedArrayListOfSampleCollection = JSONArray(arrayListOf("first item", "second item", "third item"))
        assertEquals(expectedArrayListOfSampleCollection, JSLMapper.select(".sample_collection", JSONObject(
            SAMPLE_JSON_STRING_ONE
        )))
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testSampleJsonMapper1() {
        val arrayList = arrayListOf(JSLMapper.select(".cars[0]", JSONObject(SAMPLE_JSON_STRING_TWO)), JSLMapper.select(".cars[1]", JSONObject(
            SAMPLE_JSON_STRING_TWO
        )), JSLMapper.select(".cars[2]", JSONObject(SAMPLE_JSON_STRING_TWO)))
        assertEquals(JSLMapper.select(".cars", JSONObject(SAMPLE_JSON_STRING_TWO)).toString(), JSONArray(arrayList).toString())
        assertEquals(JSLMapper.select(".cars[0]", JSONObject(SAMPLE_JSON_STRING_TWO)).toString(), JSONObject("{ \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] }").toString())
        assertEquals(JSLMapper.select(".cars[0].name", JSONObject(SAMPLE_JSON_STRING_TWO)), "Ford")
        assertEquals(JSLMapper.select(".cars[1].name", JSONObject(SAMPLE_JSON_STRING_TWO)), "BMW")
        assertEquals(JSLMapper.select(".cars[2].name", JSONObject(SAMPLE_JSON_STRING_TWO)), "Fiat")
        assertEquals(JSLMapper.select(".cars[0].models", JSONObject(SAMPLE_JSON_STRING_TWO)), JSONArray(arrayListOf("Fiesta", "Focus", "Mustang")))
        assertEquals(JSLMapper.select(".cars[1].models", JSONObject(SAMPLE_JSON_STRING_TWO)), JSONArray(arrayListOf("320", "X3", "X5")))
        assertEquals(JSLMapper.select(".cars[0].models[2]", JSONObject(SAMPLE_JSON_STRING_TWO)), "Mustang")
        assertEquals(JSLMapper.select(".cars[1].models[1]", JSONObject(SAMPLE_JSON_STRING_TWO)), "X3")
        assertEquals(JSLMapper.select(".cars[2].models[0]", JSONObject(SAMPLE_JSON_STRING_TWO)), "500")
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testThatEmptyMappingKeysErrorIsThrownForEmptyMappingString() {
        val jsl = ""
        try {
            JSLMapper.select(jsl, JSONObject(SAMPLE_JSON_STRING_ONE))
            fail("JSLMapper.select() should throw exception when the input JSON is empty")
        } catch (e: Exception) {
            assertEquals(e.toString(), RuntimeException("${JSLMapper.JSL_EMPTY_STRING} : ${JSL(jsl)}").toString())
        }
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testValueNotFoundForKeyError() {
        val jsl = ".test"
        try {
            JSLMapper.select(jsl, JSONObject(SAMPLE_JSON_STRING_ONE))
            fail("JSLMapper.select() should throw exception when there is no matching jsl in the given json")
        } catch (e: Exception) {
            assertEquals(e.toString(), RuntimeException(JSLError.ValueNotFoundForKey("test").toString()).toString())
        }
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testExpressionNotFoundError1() {
        val jsl = ".cars[0].name."
        try {
            JSLMapper.select(jsl, JSONObject(SAMPLE_JSON_STRING_TWO))
            fail("JSLMapper.select() should throw exception when there is no matching expression")
        } catch (e: Exception) {
            assertEquals(e.toString(), RuntimeException(JSLError.ExpressionNotFound("").toString()).toString())
        }
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun testInvalidJSONError() {
        val jsl = ".cars[5].name."
        try {
            JSLMapper.select(jsl, JSONObject(SAMPLE_JSON_STRING_TWO))
            fail("JSLMapper.select() should throw exception when there is invalid json based on the given jsl")
        } catch (e: Exception) {
            assertEquals(e.toString(), RuntimeException(JSLError.InvalidJSON.toString()).toString())
        }
    }

    @Config(sdk = [Build.VERSION_CODES.KITKAT])
    @Test
    fun nodeEvaluationNotSupported() {
        val jsl = "*name"
        try {
            JSLMapper.select(jsl, JSONObject(SAMPLE_JSON_STRING_ONE))
            fail("JSLMapper.select() should throw exception when there is wrong format jsl")
        } catch (e: Exception) {
            assertEquals(e.toString(), RuntimeException(
                JSLError.NodeEvaluationNotSupported("*name").toString()).toString())
        }
    }
}