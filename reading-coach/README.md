# Reading Coach (Android)

Ivana's practice for the Wonders Grade 4 tests, aimed at her two low Unit 1 scores:
the U1 W1-2 comprehension test (70%) and the U1 W4 grammar test (60%).

- **Practice test**: an original story with numbered paragraphs + 6 test-style questions
  (multiple choice, Part A / Part B text evidence, "choose two"). 8 stories: realistic fiction,
  folktales, a biography, expository texts.
- **Grammar drill**: 10 questions, mixed or by topic, following Wonders Unit 1:
  kinds of sentences, subjects & predicates, compound, complex, run-ons.
- **Fix my mistakes**: every missed question comes back until she gets it right twice.
- Every answer shows why; each round ends with a tip card for the skill she missed most.
- Progress (scores, accuracy per skill) is saved on the phone.

## Adding or changing questions
Everything lives in `app/src/main/assets/index.html` (`GRAMMAR`, `STORIES`, `TIPS` near the top of
the script). Once it is on `main`, the app downloads it on next launch. No reinstall.

## Get the APK
Pushes that change the app code run the "Build Reading Coach APK" workflow:

https://github.com/devsenweb/domain-name-generator-app/releases/download/reading-coach-latest/ReadingCoach.apk

Open that on the Android phone, tap the download, and allow "Install unknown apps" when asked.
It installs alongside Mad Minute (different app id).
