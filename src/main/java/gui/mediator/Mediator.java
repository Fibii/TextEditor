package gui.mediator;

import gui.MainController;
import gui.TabSpace;
import gui.components.MainMenuBar;
import lib.EditorUtils;

import java.nio.file.Path;
import java.util.List;

import static gui.mediator.Events.*;


public class Mediator implements IMediator {
    private Path filePath;
    private MainController mainController;
    private boolean fileSaved;
    private boolean textChanged;
    private String text;
    private MainMenuBar mainMenuBar;
    private List<TabSpace> tabSpaces;


    public static Mediator getInstance() {
        return MediatorInstance.INSTANCE;
    }

    @Override
    public void setMenuBar(MainMenuBar mainMenuBar) {
        this.mainMenuBar = mainMenuBar;
    }

    @Override
    public void setTabSpaces(List<TabSpace> tabSpaces) {
        this.tabSpaces = tabSpaces;
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * @return true if the current tab has a file that is saved, false otherwise.
     * */
    public boolean isFileSaved() {
        int tabIndex = mainController.getCurrentTabIndex();
        return tabSpaces.get(tabIndex).isFileSaved();
    }

    /**
     * checks whether to show the confirmation window when the user exits the app or not
     * by checking if all tabspaces have no changed text
     * @return AND of tabspaces.isTextChanged() ie true if any value is true in tabspaces, false otherwise
     */

    public boolean shouldExit(){
        return tabSpaces.stream()
                .anyMatch(tabSpace -> tabSpace.isTextChanged());
    }

    /**
     * @return true is the text is changed in the selected tab, false otherwise
     * */
    private boolean isTextChanged() {
        if(tabSpaces.size() == 0){
            return false;
        }
        int tabIndex = mainController.getCurrentTabIndex();
        System.out.println("mediator#isTextChanged: ----------------");
        System.out.println("tabIndex: " + tabIndex);
        System.out.println("isTextChanged: " + tabSpaces.get(tabIndex).isTextChanged());
        return tabSpaces.get(tabIndex).isTextChanged();
    }

    /**
     * @return the text in the selected tab*/
    public String getText() {
        int tabIndex = mainController.getCurrentTabIndex();
        return tabSpaces.get(tabIndex).getText();
    }

    /**
     * @return the mediator's text, used by textSpace when OPEN_MENU
     * ie gets the text from the file, to the mediator, then from the mediator, to textspace
     * */
    public String getMediatorText(){
        return text;
    }

    /**
     * @return the mediator's filepath, used by mediator to update the title when OPEN_MENU
     * */
    public Path getMediatorFilePath(){
        return filePath;
    }

    /**
     * @return the Path of the file that is opened in the selected tab
     * */
    @Override
    public Path getFilePath() {
        int tabIndex = mainController.getCurrentTabIndex();
        return tabSpaces.get(tabIndex).getCurrentPath();
    }

    private void notify(Events event) {
        int tabIndex = mainController.getCurrentTabIndex();
        switch (event) {
            case TEXT_CHANGED:
                //textChanged = true;
                tabSpaces.get(tabIndex).sendEvent(TEXT_CHANGED);
                break;
            case NEW_TAB:
                mainController.createNewTab();
                break;
            case UNDO_TEXT:
                tabSpaces.get(tabIndex).sendEvent(UNDO_TEXT);
                break;
            case OPEN_MENU:
                if(mainController.getTabPane().getTabs().size() == 0){
                    mainController.createNewTab();
                    tabIndex = mainController.getCurrentTabIndex();
                }
                tabSpaces.get(tabIndex).sendEvent(OPEN_MENU);
                mainController.updateIsSaved(fileSaved);
                updateTitles();
                break;

            case REDO_TEXT:
                tabSpaces.get(tabIndex).sendEvent(REDO_TEXT);
                break;

            case SAVE_MENU:
                tabSpaces.get(tabIndex).sendEvent(SAVE_MENU);
                updateTitles();
                break;

            case ABOUT_MENU:
                //todo: implement this
                break;
            case AUTO_SAVE:
                text = tabSpaces.get(tabIndex).getText();
                EditorUtils.writeToFile(text, filePath);
                break;

            case CLOSE_MENU:
                break;

            case EXIT_EVENT:
                text = tabSpaces.get(tabIndex).getText();
                EditorUtils.writeToFile(text, filePath);
                System.exit(0);
                break;

            case TAB_CHANGED:
                System.out.println("currentTABINDEX: " + mainController.getCurrentTabIndex());
                System.out.println("currentTAB: " + mainController.getCurrentTab());
                System.out.println(tabSpaces.get(tabIndex).getCurrentPath());
                EditorUtils.setCurrentEditorTitle(mainController.getTabPane(), tabSpaces.get(tabIndex).getCurrentPath(), mainController.getCurrentTabIndex());
                break;

            case SAVE_REQUEST:
                EditorUtils.showSaveWindow(mainController.getTabPane().getScene().getWindow());
        }
    }

    /**
     * updates the title of the tab, and stage whenever the selected tab changes
     * does nothing if no file was opened
     * */
    private void updateTitles(){
        if(getFilePath() == null){
            return;
        }
        EditorUtils.setTabTitle(mainController.getTabPane(), getFilePath(), mainController.getCurrentTabIndex());
        EditorUtils.setStageTitle(mainController.getTabPane(), getFilePath());
    }

    /**
     * returns a builder of events, used to build events
     * */
    @Override
    public EventBuilder getEventBuilder(){
        return new EventBuilder(textChanged, fileSaved, filePath, text);
    }

    private static final class MediatorInstance {
        private static Mediator INSTANCE = new Mediator();
    }

    public static class EventBuilder {

        private boolean textChanged;
        private boolean fileSaved;
        private Path filePath;
        private String text;
        private Events event;

        private EventBuilder(boolean textChanged, boolean fileSaved, Path filePath, String text) {
            this.textChanged = textChanged;
            this.fileSaved = fileSaved;
            this.filePath = filePath;
            this.text = text;
        }

        public EventBuilder textChanged(boolean textChanged) {
            this.textChanged = textChanged;
            return this;
        }

        public EventBuilder fileSaved(boolean fileSaved) {
            this.fileSaved = fileSaved;
            return this;
        }

        public EventBuilder withFilePath(Path filePath) {
            this.filePath = filePath;
            return this;
        }

        public EventBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public EventBuilder withEvent(Events event){
            this.event = event;
            return this;
        }

        public void build() {
            Mediator mediator = Mediator.getInstance();
            mediator.text = this.text;
            mediator.filePath = this.filePath;
            mediator.fileSaved = this.fileSaved;
            mediator.textChanged = this.textChanged;
            mediator.notify(event);
        }


    }

}
