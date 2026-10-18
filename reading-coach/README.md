# Reading Coach (Android)

Ivana's practice for the Wonders Grade 4 tests, aimed at her two low Unit 1 scores:
the U1 W1-2 comprehension test (70%) and the U1 W4 grammar test (60%).

- **Practice test**: an original story with numbered paragraphs + 6 test-style questions
  (multiple choice, Part A / Part B text evidence, "choose two"). 12 stories (4 per level): realistic
  fiction, folktales, biographies, expository texts.
- **Grammar drill**: 10 questions, mixed or by topic, following Wonders Unit 1:
  kinds of sentences, subjects & predicates, compound, complex, run-ons.
- **Levels** (reading and grammar each have 4). Pass a level twice to unlock the next:
  - Reading: 1 Warm-up (easier stories, paragraph highlighted) → 2 Stretch (harder stories, no
    highlight) → 3 Challenge (hardest stories, test mode: answers shown only at the end) →
    4 Test day (two stories, 12 questions, 11 right to pass).
  - Grammar: 1 Basics (8/10) → 2 Combine (9/10) → 3 Tricky: proofreading and comma traps (9/10) →
    4 Test day (12 mixed questions, test mode, 11 right to pass).
  - Lower levels can be replayed any time; only the highest unlocked level counts toward a pass.
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
