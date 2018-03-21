# Scala vs Java Dependencies

## Setup

```
sbt clean package
```

## Listing Class -> Class dependencies

We'll use [`jdeps`](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html) to enumerate
the fine-grained dependencies of each class in each JAR.

```
⚡ for i in a b c d; do \
→   JAR=$i/target/scala-2.12/${i}_2.12-0.1-SNAPSHOT.jar; \
→   echo $jar; \
→   jdeps -verbose:class $JAR; \
→ done

a_2.12-0.1-SNAPSHOT.jar -> not found
a_2.12-0.1-SNAPSHOT.jar -> /Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre/lib/rt.jar
   example.a.J1 (a_2.12-0.1-SNAPSHOT.jar)
      -> java.lang.Object
   example.a.S1 (a_2.12-0.1-SNAPSHOT.jar)
      -> java.lang.Object
      -> scala.reflect.ScalaSignature                       not found

b_2.12-0.1-SNAPSHOT.jar -> not found
b_2.12-0.1-SNAPSHOT.jar -> /Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre/lib/rt.jar
   example.b.J1 (b_2.12-0.1-SNAPSHOT.jar)
      -> example.a.J1                                       not found
      -> java.lang.Object

c_2.12-0.1-SNAPSHOT.jar -> not found
c_2.12-0.1-SNAPSHOT.jar -> /Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre/lib/rt.jar
   example.d.J1 (c_2.12-0.1-SNAPSHOT.jar)
      -> example.a.S1                                       not found
      -> java.lang.Object

d_2.12-0.1-SNAPSHOT.jar -> not found
d_2.12-0.1-SNAPSHOT.jar -> /Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre/lib/rt.jar
   example.d.J1 (d_2.12-0.1-SNAPSHOT.jar)
      -> example.a.S1                                       not found
      -> java.lang.Object
   example.d.S1 (d_2.12-0.1-SNAPSHOT.jar)
      -> example.a.S1                                       not found
      -> java.lang.Object
      -> scala.reflect.ScalaSignature                       not found
```

## Finding Scala-compiled classes

Next, we can look for marker attributes in the JARs to classify each class as being
Scala- or Java-compiled. Here, `javap` and `bash` is used to demonstrate, but it would be
be more performant and robust to parse the classfiles with [ASM](http://asm.ow2.org/).

```
⚡ for i in a b c d; do \
  JAR=$i/target/scala-2.12/${i}_2.12-0.1-SNAPSHOT.jar; \
  echo $JAR; \
  for entry in $(jar tf "$JAR" | grep -E '.*\.class$' ); do \
    javap -v -cp "$JAR" ${entry%.class} | cat -v | grep -E 'Scala: length|ScalaSig: length' > /dev/null && echo ${entry%.class} | sed -e 's/\//./g'
  done
done

a/target/scala-2.12/a_2.12-0.1-SNAPSHOT.jar
example.a.S1
b/target/scala-2.12/b_2.12-0.1-SNAPSHOT.jar
c/target/scala-2.12/c_2.12-0.1-SNAPSHOT.jar
d/target/scala-2.12/d_2.12-0.1-SNAPSHOT.jar
example.d.S1
```

## Piecing it together

Together, these reports could be used to show that `b` does not depend on APIs of Scala compiled code.

## Caveats

Static analysis of classfiles does not catch reflective use of APIs.
