package buzzword;
import apptemplate.AppTemplate;
import components.AppComponentsBuilder;
import components.AppDataComponent;
import components.AppFileComponent;
import components.AppWorkspaceComponent;
import data.GameData;
import data.GameDataFile;
import gui.Workspace;
import gui.Workspace2;
import gui.Workspace3;

/**
 * Created by CharlesGiovaniello on 11/7/16.
 */
public class BuzzWord extends AppTemplate {
    public static void main(String[] args) {
        launch(args);
    }

    public String getFileControllerClass() {
        return "BuzzWordController";
    }

    @Override
    public AppComponentsBuilder makeAppBuilderHook() {
        return new AppComponentsBuilder() {
            @Override
            public AppDataComponent buildDataComponent() throws Exception {
                return new GameData(BuzzWord.this);
            }

            @Override
            public AppFileComponent buildFileComponent() throws Exception {
                return new GameDataFile();
            }

            @Override

            public AppWorkspaceComponent buildWorkspaceComponent() throws Exception {
                return new Workspace(BuzzWord.this);
            }
            public AppWorkspaceComponent buildWorkspaceComponentlvlsel(String gameMode) throws Exception {
                return new Workspace2(BuzzWord.this, gameMode);
            }
            public AppWorkspaceComponent buildWorkspaceComponentGamePlay(String gameMode, int difficulty) throws Exception {
                return new Workspace3(BuzzWord.this, gameMode, difficulty);
            }

        };
    }
}
