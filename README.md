# mcit-helper
1. **CIS Electives Suggestion**:
    - **Graphs:** Build a graph where nodes represent courses and edges represent relationships between courses, such as prerequisites or similarities based on user interest.
    - **Graph Traversal:** Traverse the graph based on user interests to find the best matching courses. Modified Prim Algorithm will be used.
2. **Non-CIS Course Searching**:
    - **Trie:** Implement a trie data structure to efficiently store and search for course information based on department IDs, course IDs, or course names.
    - **Autocomplete:** Utilize trie traversal to provide autocomplete suggestions as the user types, enhancing the search experience.
    - **Auto suggest:** Incorporate algorithms like Levenshtein distance to suggest corrections for typos in course searches.

3. **Course Planning and Scheduling**:
    - **Hashing:** Use hashing to implement a schedule where each slot corresponds to a time slot in the user's intended course schedule.
    - **Insertion and Deletion:** Hash tables can efficiently handle insertion and deletion operations, allowing users to add or remove courses from their schedule with ease.

4. **AI-Based Mentor Matching**:
    - **Matching Algorithm:** Utilize  Gale-Shapley algorithm to match users with mentors based on their interests and the mentor's background.
    - **Mentor Retrival:** Implement B+ tree to get corresponding mentor information based on user's interest.

5. **GUI for the curriculum planner**
   - ** A GUI interface for the curriculum planner that allows users to interact with all the above features in a user-friendly manner.
