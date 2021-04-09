package pt.rocket.utils.parser

import com.json.jsonpath.ast.*
import com.json.jsonpath.isNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface JSLEvaluating {
    fun evaluate(json: Any, node: ASTNode) : Any
}

sealed class JSLError {
    data class InvalidNestedKeySequence(val v1: String) : JSLError()
    data class ValueNotFoundForKey(val v1: String) : JSLError()
    data class NodeEvaluationNotSupported(val v1: String) : JSLError()
    object InvalidJSON : JSLError()
    data class ExpressionNotFound(val v1: String) : JSLError()
    data class PrefixNotSupported(val v1: String) : JSLError()
}

@Suppress("UNCHECKED_CAST")
class JSLEvaluator: JSLEvaluating {

    @Throws(RuntimeException::class)
    override fun evaluate(json: Any, node: ASTNode) : Any {
        return when (node) {
            is Program -> evaluate(program = node, json = json)
            is ASTExpressionStatement -> evaluateExpressionStatement(node, json = json)
            is ASTSelectExpression -> evaluateSelectExpression(node, json = json)
            is ASTPrefixExpression -> evaluatePrefixExpression(node, json = json)
            is ASTIndexExpression -> evaluateIndexExpression(node, json = json)
            is ASTIntegerLiteral -> evaluateIntegerExpression(node)
            else -> throw RuntimeException(JSLError.NodeEvaluationNotSupported(node.tokenLiteral()).toString())
        }
    }

    // MARK: - Program evaluation
    private fun evaluate(program: Program, json: Any) : Any {
        var value = json
        for (statement in program.statements) {
            value = evaluate(value, statement)
        }
        return value
    }

    // MARK: - Expression Statement evaluation
    @Throws(RuntimeException::class)
    private fun evaluateExpressionStatement(statement: ASTExpressionStatement, json: Any) : Any {
        val expression = statement.getExpression() ?: throw RuntimeException(JSLError.ExpressionNotFound(statement.description).toString())
        return evaluate(json, expression)
    }

    @Throws(RuntimeException::class)
    private fun evaluatePrefixExpression(expression: ASTPrefixExpression, json: Any) : Any {
        val right = expression.getRight()
        val value = try { evaluate(json, right!!) } catch (e: Throwable) { null }
        if (right == null || value == null) {
            throw RuntimeException(JSLError.ExpressionNotFound(expression.description).toString())
        }
        when (expression.getOper()) {
            "-" -> {
                val integer = value.toString().toInt()
                if (integer.isNull()) {
                    throw RuntimeException(JSLError.InvalidJSON.toString())
                }
                return JSONObject(integer.toString())
            }
            else -> throw RuntimeException(JSLError.PrefixNotSupported(expression.getOper()).toString())
        }
    }

    @Throws(RuntimeException::class)
    private fun evaluateSelectExpression(expression: ASTSelectExpression, json: Any) : Any {
        var jsonDic: Map<String?, Any?> = HashMap()
        if (json is JSONObject) {
            jsonDic = toMap(json)
            if (jsonDic.isNull()) {
                throw RuntimeException(JSLError.InvalidNestedKeySequence(expression.key).toString())
            }
        } else if (json is HashMap<*, *>) {
            jsonDic = json as Map<String?, Any?>
        }
        var selectedValue: Any
        selectedValue = jsonDic[expression.key] ?: throw RuntimeException(JSLError.ValueNotFoundForKey(expression.key).toString())
        if (selectedValue is HashMap<*, *>) {
            selectedValue = JSONObject(selectedValue)
        } else if (selectedValue is ArrayList<*>) {
            selectedValue = JSONArray(selectedValue)
        }
        return selectedValue
    }

    @Throws(JSONException::class)
    private fun evaluateIndexExpression(expression: ASTIndexExpression, json: Any) : Any {
        val left = expression.getLeft() ?: throw RuntimeException(JSLError.ExpressionNotFound(expression.description).toString())
        val index = expression.getIndex() ?: throw RuntimeException(JSLError.ExpressionNotFound(expression.description).toString())
        val arr = evaluate(json, left) as JSONArray
        var eval = evaluate(json, index)
        eval = eval.toString().toInt()
        if (arr.length() > eval) {
            return arr[eval]!!
        }
        throw RuntimeException(JSLError.InvalidJSON.toString())
    }

    private fun evaluateIntegerExpression(expression: ASTIntegerLiteral) : Any = expression.value

    @Throws(JSONException::class)
    fun toMap(`object`: JSONObject): Map<String?, Any?> {
        val map: MutableMap<String?, Any?> = mutableMapOf()
        val keys: Iterator<*> = `object`.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            map[key] = fromJson(`object`[key])
        }
        return map
    }

    @Throws(JSONException::class)
    fun toList(array: JSONArray): List<*> {
        val list: MutableList<Any?> = ArrayList()
        for (i in 0 until array.length()) {
            list.add(fromJson(array[i]))
        }
        return list
    }

    @Throws(JSONException::class)
    fun fromJson(json: Any): Any? {
        return when {
            json === JSONObject.NULL -> {
                null
            }
            json is JSONObject -> {
                toMap(json)
            }
            json is JSONArray -> {
                toList(json)
            }
            else -> {
                json
            }
        }
    }
}
