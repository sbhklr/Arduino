package processing.app;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SketchManager {

  private Base base;

  public SketchManager(Base base) {
    this.base = base;
  }
  
  public Set<File> getRecentSketchFiles(int maxSketchCount) {
    Set<File> recentSketches = new LinkedHashSet<File>() {

      @Override
      public boolean add(File file) {
        if (size() >= maxSketchCount) {
          return false;
        }
        return super.add(file);
      }
    };

    for (String path : PreferencesData.getCollection("recent.sketches")) {
      File file = new File(path);
      if (file.exists()) {
        recentSketches.add(file);
      }
    }
    return recentSketches;
  }

  /**
   * Store list of sketches that are currently open.
   * Called when the application is quitting and documents are still open.
   */
  protected void storeSketches() {
  
    // If there is only one sketch opened save his position as default
    if (base.editors.size() == 1) {
      base.storeSketchLocation(base.editors.get(0), ".default");
    }
  
    // Save the sketch path and window placement for each open sketch
    String untitledPath = Base.untitledFolder.getAbsolutePath();
    List<Editor> reversedEditors = new LinkedList<>(base.editors);
    Collections.reverse(reversedEditors);
    int index = 0;
    for (Editor editor : reversedEditors) {
      Sketch sketch = editor.getSketch();
      String path = sketch.getMainFilePath();
      // Skip untitled sketches if they do not contains changes.
      if (path.startsWith(untitledPath) && !sketch.isModified()) {
        continue;
      }
      base.storeSketchLocation(editor, "" + index);
      index++;
    }
    PreferencesData.setInteger("last.sketch.count", index);
  }

  protected void storeRecentSketches(SketchController sketch) {
    if (sketch.isUntitled()) {
      return;
    }
  
    Set<String> sketches = new LinkedHashSet<>();
    sketches.add(sketch.getSketch().getMainFilePath());
    sketches.addAll(PreferencesData.getCollection("recent.sketches"));
  
    PreferencesData.setCollection("recent.sketches", sketches);
  }

  protected void removeRecentSketchPath(String path) {
    Collection<String> sketches = new LinkedList<>(PreferencesData.getCollection("recent.sketches"));
    sketches.remove(path);
    PreferencesData.setCollection("recent.sketches", sketches);
  }

}
