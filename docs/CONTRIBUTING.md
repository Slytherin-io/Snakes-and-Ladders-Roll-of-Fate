# 🌿 Git Workflow & Contribution Guidelines

To keep our codebase stable and our commit history readable, Team Slytherin follows a strict branching and commit strategy. **Do not push directly to the `main` branch.**

## 1. Branching Structure

* **`main`**: The production branch. Contains only tested, stable, and fully functional code.
* **`dev`**: The active integration branch. This is where the backend logic and GUI are connected and tested together.
* **Feature Branches**: Where all individual work is done. Branched off from `dev`.
* *Naming Convention:* `<type>/<short-description>`
* *Examples:* `feat/battle-system`, `ui/inventory-screen`, `fix/dice-roll-bug`

## 2. Commit Message Conventions

We follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. Every commit message must be clear and meaningful—do not make empty commits just to appear active.

**Format:** `<type>: <short description>` (all lowercase)

**Allowed Types:**

* `feat:` A new feature or major system (e.g., `feat: add snake tile mechanic`)
* `fix:` A bug fix (e.g., `fix: prevent player health from dropping below zero`)
* `ui:` Visual updates and layouts (e.g., `ui: design main menu buttons`)
* `docs:` Changes to documentation or README (e.g., `docs: update setup instructions`)
* `refactor:` Code changes that neither fix a bug nor add a feature (e.g., `refactor: clean up loop logic in EventManager`)
* `chore:` Maintenance tasks or setup (e.g., `chore: update .gitignore`)

## 3. Development Workflow (Pull Requests)

Before your code becomes part of the game, it must go through a Pull Request (PR).

1. **Create your branch:**
```bash
git checkout dev
git pull origin dev
git checkout -b <type>/<your-feature-name>

```


2. **Make your meaningful changes and commit them** using the conventions above.
3. **Push your branch to GitHub:**
```bash
git push origin <type>/<your-feature-name>

```


4. **Open a Pull Request:** Go to GitHub and open a PR to merge your feature branch into `dev`.
5. **Review:** Notify the team. Another member must review the code to ensure it doesn't break existing systems before merging.
6. **Releasing to Main:** Once the `dev` branch has a stable, tested build of the game, the Project Leader will open a PR to merge `dev` into `main`.