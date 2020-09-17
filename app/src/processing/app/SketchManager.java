package processing.app;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class SketchManager {

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

}
