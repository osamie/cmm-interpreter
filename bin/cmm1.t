# Special characters #~[]()\/ .+*:
# Escape codes include \ \t \r \n and escapes of all the special characters
# Whitespace is ignored.  Use "\ " if you really want whitespace

# Use :blah: for non-token definitions that are only used internally
# Require that these be done in an order that ensures only regular
# definitions can be created

# Reserved token types are 'skip' and 'eof'.

# Specifying the same token name twice is equivalent to a | between the
# two definition (see example of skip below)

# skip defines tokens that are consumed but not reported by the tokenizer
# Usually these are used to skip over things like whitespace and/or comments
skip: [\s\t\n\r] | //[^\r\n]*

# end of line character
eol: ;

# block delimiters
bb: {
be: }


# parentheses
lparen: \(
rparen: \)

# list separator
listsep: ,

# logical operators
and: &
or: \|

# arithmetic operators
plus: \+
minus: -
multiply: \*
divide: /
mod: %
exp: \^

# comparison operators
eq: ==
lt: <
gt: >
le: <=
ge: >=
ne: != | <>
gets: =

# keywords
do: do
while: while
if: if
elsif: elsif
else: else
return: return
number_t: number
string_t: string
boolean_t: boolean

# string constants
string: "([^"]|\\")*"

# numeric constants
:integer: [0123456789]+
:float: :integer: (\.:integer:)? | \.:integer:
number: -? (:float: | :float: [eE] [-\+]? :integer:?)

# boolean constants
boolean: true | false

# identifiers
:letter: [ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_]
:digit: [0123456789]
id: :letter: (:digit:|:letter:)*

