# Probe

# (Work in progess)

Probe is a dynamically typed language.

Heavily inspired from Lox language described [here](http://craftinginterpreters.com/)

Example :

```javascript
// Welcome to Probe

//Customary hello world
print "hello, world";

fn add(x, y) {
    return x + y;
}

print add(6, 24); //30

var arr = [1, 2, "hello", [10, 11], 5];

print arr; // [1, 2, "hello", [10, 11], 5]
print arr[3]; // [10, 11]

var holder = fn() { // anonymous function
    var x = 1;
    return fn(y){
        var result = y * x;
         x = x + 1;
         return result;
    };
};

var closureDemo = holder();
for(var i=1; i < 10 ; i = i + 1 ){
    print closureDemo(i); // prints squares of numbers from 1 to 9
}

print 2 + 5 * 3 + ( 9 - 2 ) * 4; // 45

print "hello, " + "world"; // string concatenation

//Variables are block scoped
var _x = 1;
{
    var _x = 3;
    {
        var _x = 4;
        print _x; //4
    }
    print _x; //3
}
print _x; //1

fn map(arr, mapper) {
    for(var i = 0; i < len(arr) ; i = i + 1 ){ // len is an in built function to find length of array or string
        print mapper(arr[i]);
    }
}

map([1, 2, 3, 4, 5], fn(x) {
    return x * 5;
});

fn fib(n) {
    if(n==0 or n==1) return n;
    return fib(n-1) + fib(n-2);
}

print fib(8); // recursion

print !true; // false

var a; // a is nil
print a; // nil


```