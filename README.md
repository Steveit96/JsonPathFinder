# JSL

This is the reference implementation of the JSON Selector Language. JSL is a
query language that allows the selection of values from JSON. JSL was designed to run
in client environments, such as the mobile apps or the web. It is
intentionally a lightweight, simple language.

Using this JSON as input, we can assume the following: 

```
{
    "data": {
        "token": "123", 
        "items": ["a", "b", "c"]
    }
}
```

* The JSL `.data.items` will evaluate to `["a", "b", "c"]`.
* The JSL `.data.items[1]` will evaluate to `"b"`.
* The JSL `.data.token` will evaluate to `123`.
* The JSL `.stuff` will not evaluate to anything and will error out. The key
  "stuff" doesn't exist.


## Grammar

* Select expressions select the key \<identifier\> in the current indentation
level of the JSON.
* Select expressions can be chained to represent nested JSON.
* The current implementation does not allow escaping of tokens, for instance the
  dot.

* Index expressions select an entry from a JSON array at the index
\<numeric literal\>.
* Index expressions must be preceded by Select expressions.
* An index expression can only be evaluated when the underlying selected JSON is
  an array.

Both of these expressions evaluate to any valid JSON.


## Tour of this Proof of Concept

### `Lexer.kt`

Performs lexical analysis on raw string input.

### `Parser.kt`

Parses the lexed tokens into an abstract syntax tree

### `Token.kt`

The available token types in the JSL syntax.

### `AST.kt`

Various types of AST nodes. The AST is a tree of statements. This language only
has expressions at the moment, so you'll notice all important nodes except the
root are of type ASTExpressionStatement.

### `Program.kt`

The root node of the AST. The final parsed AST will be of this type.

### `JSL.kt`

A nice high-level wrapper around everything above. Usage:

## Guidance

* Run the tests. See that all tests pass.
* Create a `JSLEvaluator` type that takes in the target JSON, the `Program`,
  and responds with valid JSON or errors. This evaluator is basically like a
  language interpreter. JSL at the moment doesn't have an environment or object
  model, so creating an evaluator should be easy. It's stateless.
* You can optimize performance by making `Program` and all AST nodes conform
  to `Codable` and archive the `Program` based on the raw JSL input. That way
  you don't have to parse every time (like the current `split` and regex
  approach is doing).
