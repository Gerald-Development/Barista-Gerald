# Contributing to Barista-Gerald
Before anything, thank you for contributing to Gerald. It's a massive help!

The following lays out some basic guidelines you must follow to ensure your PR's & Additions are as
clean as possible. This will allow for smooth merges & easy deployment of your code. As well as these rules,
using your best judgement will also help to ensure everything goes as smoothly as possible. Feel free
to propose changes to this document. 


# How can I contribute?
## Reporting bugs
This section can guide you through on how to report a bug to the core developers.
#### Before reporting a bug
Before any bug is reported, ensure that you're doing everything you can to make sure it isn't a localised 
issue. Making sure that your GeraldConfig.properties file is set up correctly. A common mistake is the 
postgres username & password. Check these first if the issue is related to postgres.

#### How do I submit a (good) Bug Report?
Bugs are tracked as [GitHub issues](https://guides.github.com/features/issues/). All bug reports should
be related to the Main branch. Developer branch is inherently untested & therefore can have issues. 
Should you see a vital error in the dev branch, either wait to see if it's still there when merged in main,
or submit a PR fixing it yourself. Currently, there is no template for bug reports. How this is to come.

Explain the problem and include additional details to help maintainers reproduce the problem:
* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible. For example, 
  start by explaining how you started Atom, e.g. which command exactly you used in the terminal, 
  or how you started Atom otherwise. When listing steps, **don't just say what you did, but explain 
  how you did it**. For example, if you moved the cursor to the end of a line, 
  explain if you used the mouse, or a keyboard shortcut or an Atom command, and if so which one?
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, 
  or copy/pasteable snippets, which you use in those examples. If you're providing snippets in the issue, 
  use [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the behavior you observed after following the steps** and point out 
  what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include screenshots and animated GIFs** which show you following the described steps and clearly 
  demonstrate the problem. If you use the keyboard while following the steps, 
  **record the GIF with the [Keybinding Resolver](https://github.com/atom/keybinding-resolver) shown**. 
  You can use [this tool](https://www.cockos.com/licecap/) to record GIFs on macOS and Windows, 
  and [this tool](https://github.com/colinkeenan/silentcast) or 
  [this tool](https://github.com/GNOME/byzanz) on Linux.
  
## Suggesting New Enhancements.
This section can guide you through on how to suggest enhancements to maintainers. This can be a new feature,
minor improvements to existing functionality within the bot. Following the rules below will help ensure
your enhancement is taken seriously & put into consideration by our maintainers. 

#### Before submitting An Enhancement Suggestion
- Check to see if you're using the latest version of Barista Gerald.
- Check if there's already a Pull Request with the enhancement you're looking for.
- Also check if its already been suggested.

### How do I submit a (good) Enhancement?
Enhancements are tracked as [GitHub issues](https://guides.github.com/features/issues/). All enhancements 
should be relating to the Main branch. Not dev branches. Use the following list to ensure your
suggestion is as easy to understand as possible.
* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include copy/pasteable 
  snippets which you use in those examples, as 
  [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Include screenshots and animated GIFs** which help you demonstrate the steps which the suggestion 
  is related to. You can use [this tool](https://www.cockos.com/licecap/) 
  to record GIFs on macOS and Windows, and 
  [this tool](https://github.com/colinkeenan/silentcast) or 
  [this tool](https://github.com/GNOME/byzanz) on Linux.
  
## Submitting your First Code Contribution.
 Currently, Code Contributions don't have a robust layout for where to find beginner issues, use your
 best judgement to determine if you have the skill set required to fix an issue or implement a new feature.
 ### Local development.
First ensure that you have your own Gerald-Debug bot. Go to 
[Discord applications](https://discord.com/developers/applications) and follow the normal procedures to 
create your bot. Refer to the [README](https://github.com/Montori/Barista-Gerald/blob/main/README.md) for
setup of your development bot.

### Pull requests.
The process described here has several goals. 
- Maintain Gerald's Quality
- Fix problems that are important to users
- Engage the community in working toward the best possible Gerald
- Enable a sustainable system for Gerald's maintainers to review contributions

While the prerequisites above must be satisfied prior to having your pull request reviewed, the reviewer(s) 
may ask you to complete additional design work, tests, or other changes before your pull request can be 
ultimately accepted.

## Styling Guidelines
### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Reference issues and pull requests liberally after the first line
  
(more to come)