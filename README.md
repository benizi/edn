# edn

Super simple edn to JSON converter.

# Intro

[edn][edn] is a data notation that basically looks like Clojure data.  I often
need to generate JSON, but I vastly prefer using edn, because it has so much
less syntactic noise.  So, I wrote a straightforward program to accept edn on
stdin and generate JSON on stdout.

# Background

When I originally thought of doing this, I was pretty annoyed at JVM Clojure's
startup time for what is mainly a scripting tool for me.  So, I tried to get
[Lumo][lumo] to run it fast enough that the startup time wasn't an issue.  It
was reasonably fast, but not great.

Then, much later, I came across ["Command-line apps with Clojure and GraalVM:
300x better start-up times"][blog-post], a nice walk-through of compiling JVM
Clojure to a native binary.  So, I extracted the script from my dotfiles.

(Dotfiles versions are still available in the [dotfiles branch][attic].)

# Usage

## With Boot

If you work with Clojure, you may already have [`boot`][boot] installed.  If
so, `build.boot` is an executable script you can run directly:

```sh
$ ./build.boot
{a one b {two 42} la-dee dah}
# =>
{"a":"one","b":{"two":42},"la-dee":"dah"}
```

## Compiled

Much better is to compile it to a native binary using [GraalVM][graalvm].  It
still requires you to have [`boot`][boot] installed.

One-time build step:

```sh
$ boot build
Compiling 1/1 com.benizi.edn...
Adding uberjar entries...
Writing com.benizi.edn.jar...
Sifting output files...
Writing target dir(s)...
   classlist:   3,933.25 ms
       (cap):   1,569.82 ms
       setup:   3,005.85 ms
  (typeflow):  11,268.54 ms
   (objects):   5,464.71 ms
  (features):      90.36 ms
    analysis:  17,074.67 ms
    universe:     530.26 ms
     (parse):   4,160.07 ms
    (inline):  23,597.69 ms
   (compile):  38,542.37 ms
     compile:  67,116.42 ms
       image:   2,588.96 ms
       write:     453.12 ms
     [total]:  94,821.24 ms
Writing target dir(s)...
```

Then you can install `target/edn` wherever you'd like.

```sh
$ edn
{a one b {two 42} la-dee dah}
# =>
{"a":"one","b":{"two":42},"la-dee":"dah"}
```

# Features

- [x] Reads edn forms from stdin, outputs JSON to stdout
- [x] Can compile to native binary using GraalVM

# License

Copyright © 2018 Benjamin R. Haskell

Distributed under the MIT License[¹][¹] (included in file: [LICENSE](LICENSE)).

---

[edn]: https://github.com/edn-format/edn
[lumo]: https://github.com/anmonteiro/lumo
[boot]: http://boot-clj.com/
[graalvm]: https://www.graalvm.org/
[blog-post]: https://www.astrecipes.net/blog/2018/07/20/cmd-line-apps-with-clojure-and-graalvm/
[attic]: https://github.com/benizi/edn/tree/dotfiles
[¹]: 90% sure this is fine w.r.t. the Eclipse Public License common in Clojure-land.
