package javase.chapter1;

import java.lang.Integer; //The TypeName must be the canonical name of a class, interface
import java.util.Integer; //The type must be either a member of a named package
import myUtil.Integer; //compile-time error because of the duplicate declaration of Integer
import myUtil.Imports; //declares a top level type Imports, a compile-time error occurs.
import java.lang.Integer; //the duplicate declaration is ignored.
import java.lang.*; //The type-import-on-demand declaration is ignored in such cases.
import java.lang.*; //the duplicate declaration is ignored.
import static java.lang.Integer.MAX_VALUE; //the duplicate declaration is ignored.
import static java.lang.Integer.*; //the duplicate declaration is ignored.
import static java.lang.Integer.*; //the duplicate declaration is ignored.
static import java.lang.Integer.*; //first token must be import

//top level class
public class Imports {

}