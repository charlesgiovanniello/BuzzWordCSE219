package components;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This interface provides the structure for file components in
 * our applications. Note that by doing so we make it possible
 * for customly provided descendent classes to have their methods
 * called from this framework.
 *
 * @author Richard McKenna, Ritwik Banerjee
 */
public interface AppFileComponent {

    void saveData(AppDataComponent data, Path filePath) throws IOException;

    void createProfile(AppDataComponent data, Path filePath, String userName, String passWord) throws IOException;

    boolean login(AppDataComponent data, Path filePath, String userName, String passWord) throws IOException;

    void loadData(AppDataComponent data, Path filePath) throws IOException;

    void updateUserProg(AppDataComponent data, Path to, String mode, String userName, int difficulty) throws IOException;

    void exportData(AppDataComponent data, Path filePath) throws IOException;
}
