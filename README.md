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

# UUID-handling

Another thing I often need in generating JSON is UUIDs, but I hate shelling out
to `uuidgen` all the time. So, I added the following custom conversions for
`#uuid`-tagged edn literals:

- `#uuid value` where `value` is a keyword, number, or symbol, generates a
  random UUID on each run of the program, but if you use the same keyword
  twice, you'll get the same value.

  E.g., each of the following outputs a JSON array of two identical UUIDs:

    [#uuid :a #uuid :a]
    [#uuid a #uuid a]
    [#uuid 1 #uuid 1]

  Keywords and symbols are cached separately, so this results in a JSON Array
  of two distinct UUIDs:

    [#uuid :a #uuid a]

- `#uuid nil` generates a random UUID every time

    [#uuid nil #uuid nil] -> JSON Array of two distinct UUIDs

- `#uuid [value]` or `#uuid [value nybble-length]` allows specifying the hex
  value as a number. `nybble-length` is optional, and defaults to 32. If it's
  less than 32, the hex representation is repeated to fill 32 nybbles,
  right-aligned (i.e., trimmed from the left).

    #uuid [1]   -> #uuid "00000000-0000-0000-0000-000000000001"
    #uuid [1 4] -> #uuid "00010001-0001-0001-0001-000100010001"
    #uuid [1 3] -> #uuid "01001001-0010-0100-1001-001001001001"

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
- [x] Use a custom reader for `#uuid`-tagged values

# License

Copyright © 2018-2019 Benjamin R. Haskell

Distributed under the MIT License¹ (included in file: [LICENSE](LICENSE)).

---

¹: 90% sure this is fine w.r.t. the Eclipse Public License common in Clojure-land.

[edn]: https://github.com/edn-format/edn
[lumo]: https://github.com/anmonteiro/lumo
[boot]: http://boot-clj.com/
[graalvm]: https://www.graalvm.org/
[blog-post]: https://www.astrecipes.net/blog/2018/07/20/cmd-line-apps-with-clojure-and-graalvm/
[attic]: https://github.com/benizi/edn/tree/dotfiles
