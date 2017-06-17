Contributing
============

Thank you for your interest in contributing to WorldEdit! We appreciate your 
effort, but to make sure that the inclusion of your patch is a smooth process, we
ask that you make note of the following guidelines.

* **Follow the [Oracle coding conventions](http://www.oracle.com/technetwork/java/codeconv-138413.html).**
  We can't stress this enough; if your code has notable issues, it may delay
  the process significantly.
* **Target Java 7 for source and compilation.** Make sure to mark methods with
  ` @Override` that override methods of parent classes, or that implement
  methods of interfaces.
* **Use only spaces for indentation.** Our indents are 4-spaces long, and tabs
  are unacceptable.
* **Wrap code to a 120 column limit.** We do this to make side by side diffs
  and other such tasks easier. Ignore this guideline if it makes the code
  too unreadable.
* **Write complete Javadocs.** Do so only for public methods, and make sure
  that your `@param` and `@return` fields are not just blank.
* **Don't tag classes with @author.** Some legacy classes may have this tag,
  but we are phasing it out.
* **Make sure the code is efficient.** One way you can achieve this is to spend
  around ten minutes to think about what the code is doing and whether it
  seems awfully roundabout. If you had to copy the same large piece of
  code in several places, that's bad.
* **Keep commit summaries under 70 characters.** For more details, place two
  new lines after the summary line and write away!
* **Test your code.** We're not interested in broken code, for the obvious reasons.
* **Write unit tests.** While this is strictly optional, we recommend it for
  complicated algorithms.


Checklist
---------

Ready to submit? Perform the checklist below:

1. Have all tabs been replaced into four spaces? Are indentations 4-space wide?
2. Have I written proper Javadocs for my public methods? Are the @param and
   @return fields actually filled out?
3. Have I `git rebase`d my pull request to the latest commit of the target
   branch?
4. Have I combined my commits into a reasonably small number (if not one)
   commit using `git rebase`?
5. Have I made my pull request too large? Pull requests should introduce
   small sets of changes at a time. Major changes should be discussed with
   the team prior to starting work.
6. Are my commit messages descriptive?

You should be aware of [`git rebase`](http://learn.github.com/p/rebasing.html).
It allows you to modify existing commit messages, and combine, break apart, or
adjust past changes.

Example
-------

This is **GOOD:**

    if (var.func(param1, param2)) {
        // do things
    }

This is **EXTREMELY BAD:**

    if(var.func( param1, param2 ))
    {
        // do things
    }
