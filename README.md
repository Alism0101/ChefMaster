# 👨‍🍳 Chef Vault (ChefMaster)

##  Overview
Chef Vault is a native Android application designed to act as a digital recipe book. It allows users (chefs and cooking enthusiasts) to easily create, store, organize, and manage their favorite recipes. Built with a focus on seamless user experience, the app features persistent local data storage, ensuring no recipe is ever lost.

##  Key Features
* **Full CRUD Functionality:** Create, Read, Update, and Delete recipes effortlessly.
* **Persistent Local Storage:** Utilizes Android's Room Database (SQLite) to save recipes permanently on the device.
* **Smart Search & Sort:** * Search for recipes dynamically by Title or Ingredients.
  * Sort the vault by "Newest First" or "Alphabetical (A-Z)".
* **Favorites System:** Mark go-to recipes with a star for quick access and filter the list to view only favorites.
* **Recycle Bin (Trash):** A built-in fail-safe that moves deleted recipes to a "Trash" folder. Users can either restore them to the vault or permanently delete them.
* **Quick Share:** Share recipes (Title and Ingredients) directly to other apps (WhatsApp, Email, etc.) via Android Intents.
* **Timestamp Tracking:** Automatically tracks and displays when a recipe was "Last Edited".

## 🛠️ Tech Stack
* **Language:** Java
* **UI/Layout:** XML
* **Database:** Room Database (SQLite abstract layer)
* **Architecture/Components:** RecyclerView, Data Access Objects (DAO), Android Intents
* **IDE:** Android Studio

##  Getting Started

To clone and run this project locally, you will need to have [Android Studio](https://developer.android.com/studio) installed on your machine.

1. Clone the repository:
   ```bash
   git clone [https://github.com/Alism0101/ChefMaster.git](https://github.com/Alism0101/ChefMaster.git)

2. Open Android Studio.

3. Click on File > Open and select the cloned ChefMaster folder.

4. Allow Gradle to sync and build the project.

5. Click the Run button (green play icon) to launch the app on an emulator or a connected physical Android device.

Author
L. Ali Ismail Khan

[LinkedIn Profile] https://www.linkedin.com/in/l-ali-ismail-khan-28b528253/
