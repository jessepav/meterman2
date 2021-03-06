package com.illcode.meterman2;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.event.*;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.state.AttributeSetPermuter;
import com.illcode.meterman2.state.GameState;
import com.illcode.meterman2.text.Markup;
import com.illcode.meterman2.text.TextSource;
import com.illcode.meterman2.ui.UIConstants;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.SwingUtilities;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.illcode.meterman2.GameUtils.hasAttr;
import static com.illcode.meterman2.GameUtils.setAttr;
import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.ui;
import static com.illcode.meterman2.SystemAttributes.EQUIPPABLE;
import static com.illcode.meterman2.SystemAttributes.VISITED;

public final class GameManager
{
    private String outputSeparator;

    private Game game;  // The game we're currently playing
    private Player player;
    private Map<String,Object> gameStateMap;
    private Map<String,Entity> entityIdMap;
    private Map<String,Room> roomIdMap;

    private Room currentRoom;
    private Entity selectedEntity;  // currently selected entity, or null if none
    private int numTurns;

    private EventHandlerManager handlerManager;
    private GameObjectProperties objectProps;

    private StringBuilder outputBuilder;  // To be used in composing text before sending it off to the UI.
    private StringBuilder transcript;  // the game transcript

    private StringBuilder commonTextBuilder, paragraphBuilder; // See queueLookText()
    private List<Action> actions; // Used for composing UI actions - reuse same list to avoid allocation
    private boolean alwaysLook; // see setAlwaysLook()

    // See processChangedObjects()
    private Set<Entity> changedEntities;
    private List<Entity> entityProcessingList;
    private Set<Room> changedRooms;
    private List<Room> roomProcessingList;

    // To update our UI at the transition of turns
    private boolean roomRefreshNeeded;
    private boolean entityRefreshNeeded;
    private boolean inventoryRefreshNeeded;
    private boolean lookNeeded;

    private boolean endGameSignalled;

    private List<Entity> entityList;  // temporary list to avoid allocation; always clear() after using

    private StatusBarProvider statusBarProvider;

    GameManager() {
        handlerManager = new EventHandlerManager();
        objectProps = new GameObjectProperties();
        putBinding("props", objectProps);

        outputBuilder = new StringBuilder(2048);
        transcript = new StringBuilder(262144);
        commonTextBuilder = new StringBuilder(1024);
        paragraphBuilder = new StringBuilder(1024);

        actions = new ArrayList<>(16);

        changedEntities = new HashSet<>();
        entityProcessingList = new ArrayList<>();
        changedRooms = new HashSet<>();
        roomProcessingList = new ArrayList<>();

        entityList = new ArrayList<>();

        outputSeparator = bundles.getPassage("output-separator").getText() + "\n";
        alwaysLook = Utils.booleanPref("always-look", true);
    }


    void dispose() {
        closeGame();

        player = null;
        handlerManager = null;
        objectProps = null;
        outputBuilder = null;
        transcript = null;
        commonTextBuilder = null;
        paragraphBuilder = null;
        actions = null;
        changedEntities = null;
        changedRooms = null;
        roomProcessingList = null;
        entityList = null;
    }

    /**
     * Start a new game, and perform an initial "Look" command.
     * @param game game to start
     */
    void newGame(Game game) {
        closeGame();
        this.game = game;
        final String gameName = game.getName();
        ui.showWaitDialog("Starting " + gameName + "...");
        ui.setGameName(gameName);
        ui.setFrameImageVisible(Meterman2.gamesList.getGameFrameImageVisible(gameName));
        Meterman2.assets.setGameAssetsPath(Meterman2.gamesList.getGameAssetsPath(gameName));
        final String packageName = Meterman2.gamesList.getGamePackageName(gameName);
        if (!packageName.isEmpty())
            Meterman2.script.importPackage(packageName);
        ui.clearText();
        game.init();
        gameStateMap = game.getInitialGameStateMap();
        putBindings(gameStateMap);
        putBinding("game", game);
        game.constructWorld(true);
        entityIdMap = game.getEntityIdMap();
        roomIdMap = game.getRoomIdMap();
        numTurns = 0;
        player = game.getStartingPlayer();
        currentRoom = game.getStartingRoom();
        game.registerInitialGameHandlers();
        selectedEntity = null;

        putBinding("currentRoom", currentRoom);
        putBinding("entities", entityIdMap);
        putBinding("rooms", roomIdMap);

        queueRoomUIRefresh();
        queueInventoryUIRefresh();
        queueEntityUIRefresh();
        ui.setGlobalActionButtonText();
        ui.setFrameImage(UIConstants.DEFAULT_FRAME_IMAGE);
        ui.hideWaitDialog();
        for (Entity e : entityIdMap.values())
            e.gameStarting();
        for (Room r : roomIdMap.values())
            r.gameStarting();
        handlerManager.fireGameStarting(true);
        game.start(true);
        currentRoom.entered(null);
        lookCommand();
        setAttr(currentRoom, VISITED);
    }

    void loadGame(final Game game, final GameState state) {
        closeGame();
        this.game = game;
        final String gameName = game.getName();
        ui.setGameName(gameName);
        ui.setFrameImageVisible(Meterman2.gamesList.getGameFrameImageVisible(gameName));
        Meterman2.assets.setGameAssetsPath(Meterman2.gamesList.getGameAssetsPath(gameName));
        final String packageName = Meterman2.gamesList.getGamePackageName(gameName);
        if (!packageName.isEmpty())
            Meterman2.script.importPackage(packageName);
        game.init();
        player = new Player();
        gameStateMap = state.gameStateMap;
        game.setGameStateMap(gameStateMap);
        putBindings(gameStateMap);
        putBinding("game", game);
        game.constructWorld(false);
        entityIdMap = game.getEntityIdMap();
        roomIdMap = game.getRoomIdMap();

        restoreGameObjectProperties(entityIdMap, roomIdMap, player, state);
        restoreHandlers(state, game);
        for (List<? extends GameEventHandler> handlerList : handlerManager.getEventHandlerMap().values())
            for (GameEventHandler h : handlerList)
                h.restoreHandlerState(state.handlerStateMap.get(h.getHandlerId()));
        currentRoom = roomIdMap.get(state.currentRoomId);
        numTurns = state.numTurns;
        selectedEntity = null;

        putBinding("currentRoom", currentRoom);
        putBinding("entities", entityIdMap);
        putBinding("rooms", roomIdMap);

        queueRoomUIRefresh();
        queueInventoryUIRefresh();
        queueEntityUIRefresh();
        ui.setGlobalActionButtonText();
        ui.setFrameImage(UIConstants.DEFAULT_FRAME_IMAGE);
        ui.hideWaitDialog();
        for (Entity e : entityIdMap.values())
            e.gameStarting();
        for (Room r : roomIdMap.values())
            r.gameStarting();
        handlerManager.fireGameStarting(false);
        game.start(false);
        outputText();  // since no nextTurn() is called, we update the text area...
        refreshUI();   // ...and UI ourselves.
    }

    private void closeGame() {
        handlerManager.clearListenerLists();
        objectProps.clear();
        player = null;
        currentRoom = null;
        gameStateMap = null;
        entityIdMap = null;
        roomIdMap = null;
        changedEntities.clear();
        changedRooms.clear();
        roomRefreshNeeded = false;
        entityRefreshNeeded = false;
        inventoryRefreshNeeded = false;
        outputBuilder.setLength(0);
        transcript.setLength(0);

        if (game != null) {
            game.dispose();
            game = null;
        }
        ui.clearImages();
        ui.setFrameImageVisible(true);
        ui.setGameName(null);
        ui.setRoomName("");
        ui.clearStatusLabels();
        Meterman2.sound.clearAudio();
        Meterman2.script.clearBindings();
        Meterman2.template.clearBindings();
        Meterman2.template.clearGameTemplates();
        Meterman2.template.clearTemplateCache();
        Meterman2.attributes.clearGameAttributes();
        for (Action a : Meterman2.actions.getGameActions())
            ui.removeActionBinding(a);
        Meterman2.actions.clearGameActions();
        Meterman2.bundles.clearGameBundles();
        Meterman2.assets.setGameAssetsPath(null);
        ui.setGlobalActionButtonText();  // we need to do this last because system action text was reset
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public int getNumTurns() {
        return numTurns;
    }

    /** Return the entity with the given id, or null if not found. */
    public Entity getEntity(String id) {
        return entityIdMap.get(id);
    }

    /** Return the room with the given id, or null if not found. */
    public Room getRoom(String id) {
        return roomIdMap.get(id);
    }

    /** Return the game state object with the given name, or null if not found. */
    public Object getGameStateObject(String name) {
        return gameStateMap.get(name);
    }

    /** Return the custom object properties instance being used. */
    public GameObjectProperties objectProps() {
        return objectProps;
    }

    /** Set whether we should always "Look" when entering a room, even if it's been visited before. */
    public void setAlwaysLook(boolean alwaysLook) {
        this.alwaysLook = alwaysLook;
    }

    /** @see #setAlwaysLook(boolean) */
    public boolean isAlwaysLook() {
        return alwaysLook;
    }

    /**
     * Moves the player to a destination room. All appropriate listeners will be notified, and
     * one of them may cancel this move.
     * @param toRoom the room to which the player should move.
     */
    public void movePlayer(Room toRoom) {
        if (toRoom == null || toRoom == currentRoom)
            return;
        // Here we go...
        Room fromRoom = currentRoom;
        if (handlerManager.firePlayerMovement(fromRoom, toRoom, true))
            return; // we were blocked by a listener
        if (fromRoom.exiting(toRoom))
            return;  // blocked by the room itself
        GameUtils.gatherEntitiesRecursive(fromRoom, entityList);
        for (Entity e : entityList)
            e.exitingScope();
        entityList.clear();
        currentRoom = toRoom;  // we've moved!
        putBinding("currentRoom", currentRoom);
        toRoom.entered(fromRoom);
        GameUtils.gatherEntitiesRecursive(toRoom, entityList);
        for (Entity e : entityList)
            e.enterScope();
        entityList.clear();
        handlerManager.firePlayerMovement(fromRoom, toRoom, false);
        ui.clearEntitySelection();  // this in turn will call entitySelected(null) if needed
        if (alwaysLook || !hasAttr(toRoom, SystemAttributes.VISITED))
            lookNeeded = true;
        setAttr(toRoom, SystemAttributes.VISITED);
        queueRoomUIRefresh();
    }

    /**
     * Move an entity to a given container (which may include the player).
     * @param e entity to move
     * @param toContainer destination container
     */
    public void moveEntity(Entity e, EntityContainer toContainer) {
        final EntityContainer fromContainer = e.getContainer();
        if (fromContainer == toContainer)
            return;
        final boolean inInventoryBefore = GameUtils.isParentContainer(player, fromContainer);
        final boolean inInventoryAfter = GameUtils.isParentContainer(player, toContainer);
        if (inInventoryBefore) {
            if (!inInventoryAfter) {
                player.unequipEntity(e);
                e.dropped();
            }
            queueInventoryUIRefresh();
        }
        final boolean inScopeBefore = GameUtils.getRoom(fromContainer) == currentRoom;
        final boolean inScopeAfter = GameUtils.getRoom(toContainer) == currentRoom;
        if (inScopeBefore && !inScopeAfter) {
            entityList.add(e);
            if (e instanceof EntityContainer)
                GameUtils.gatherEntitiesRecursive((EntityContainer) e, entityList);
            for (Entity _e : entityList)
                _e.exitingScope();
            entityList.clear();
        }
        // Now move the entity.
        GameUtils.putInContainer(e, toContainer);
        // done moving!
        if (!inScopeBefore && inScopeAfter) {
            entityList.add(e);
            if (e instanceof EntityContainer)
                GameUtils.gatherEntitiesRecursive((EntityContainer) e, entityList);
            for (Entity _e : entityList)
                _e.enterScope();
            entityList.clear();
        }
        if (fromContainer == currentRoom || toContainer == currentRoom)
            queueRoomUIRefresh();
        if (e == selectedEntity)
            entitySelected(null);
        if (inInventoryAfter) {
            if (!inInventoryBefore)
                e.taken();
            queueInventoryUIRefresh();
        }
    }

    /** Returns true if the given entity is in the player inventory. */
    public boolean isInInventory(Entity e) {
        return e != null && e.getContainer() == player;
    }

    /** Returns true if the entity is equipped by the player */
    public boolean isEquipped(Entity e) {
        return player.getEquippedEntities().contains(e);
    }

    /**
     * Sets whether an entity is equipped by the player.
     * @param e entity to equip
     * @param equip whether the player should equip the entity. If true, and the given entity is in
     *      the player inventory and is {@link SystemAttributes#EQUIPPABLE equippable},
     *      the player will equip it.
     * @return true if the operation succeeded
     */
    public boolean setEquipped(Entity e, boolean equip) {
        final Collection<Entity> equippedEntities = player.getEquippedEntities();
        if (equip) {
            if (hasAttr(e, EQUIPPABLE) && isInInventory(e) && !equippedEntities.contains(e)) {
                equippedEntities.add(e);
                queueInventoryUIRefresh();
                return true;
            }
        } else {
            if (equippedEntities.remove(e)) {
                queueInventoryUIRefresh();
                return true;
            }
        }
        return false;
    }

    // Indicate the entity UI should be refreshed at the end of turn.
    private void queueEntityUIRefresh() {
        entityRefreshNeeded = true;
    }

    private void refreshEntityUI() {
        if (selectedEntity != null) {
            actions.clear();
            actions.addAll(selectedEntity.getActions());
            handlerManager.fireProcessEntityActions(selectedEntity, actions);
            ui.clearActions();
            for (Action a : actions)
                ui.addAction(a);
        } else {
            ui.clearActions();
            ui.setEntityImage(UIConstants.NO_IMAGE);
        }
    }

    // Indicate the room UI should be refreshed at the end of turn.
    private void queueRoomUIRefresh() {
        roomRefreshNeeded = true;
    }

    private void refreshRoomUI() {
        ui.setRoomName(GameUtils.getRoomName(currentRoom));
        for (int pos = 0; pos < UIConstants.NUM_EXIT_BUTTONS; pos++)
            ui.setExitLabel(pos, currentRoom.getExitLabel(pos));
        Entity savedSE = selectedEntity;
        ui.clearRoomEntities();
        for (Entity e : GameUtils.getRoomEntities(currentRoom))
            if (!e.getAttributes().get(SystemAttributes.CONCEALED))
                ui.addRoomEntity(e.getId(), e.getName());
        if (savedSE != null)
            ui.selectEntity(savedSE.getId());
    }

    // Indicate the inventory UI should be refreshed at the end of turn.
    private void queueInventoryUIRefresh() {
        inventoryRefreshNeeded = true;
    }

    private void refreshInventoryUI() {
        Entity savedSE = selectedEntity;
        List<Entity> inventory = player.getEntities();
        Collection<Entity> equippedItems = player.getEquippedEntities();
        ui.clearInventoryEntities();
        // We make two passes through the player inventory, first adding equipped items, and then the rest.
        for (Entity item : inventory) {
            if (equippedItems.contains(item))
                ui.addInventoryEntity(item.getId(), item.getName() + " (e)");
        }
        for (Entity item : inventory) {
            if (!equippedItems.contains(item))
                ui.addInventoryEntity(item.getId(), item.getName());
        }
        if (savedSE != null)
            ui.selectEntity(savedSE.getId());
    }

    /** Called by the UI when the user clicks "Look", or when the player moves rooms */
    public void lookCommand() {
        performLook();
        nextTurn();
    }

    /**
     * Actually performs the look command, but does not output buffered text.
     */
    private void performLook() {
        newPar();
        outputBuilder.append(GameUtils.getRoomDescription(currentRoom));
        outputBuilder.append("\n");
        for (Entity e : GameUtils.getRoomEntities(currentRoom))
            e.lookInRoom();
        handlerManager.fireLookPerformed(currentRoom);
        if (commonTextBuilder.length() != 0) {
            outputBuilder.append('\n').append(commonTextBuilder).append('\n');
            commonTextBuilder.setLength(0);
        }
        if (paragraphBuilder.length() != 0) {
            outputBuilder.append(paragraphBuilder);
            paragraphBuilder.setLength(0);
        }
    }

    /**
     * Adds text to be shown during the next look action.
     * @param text text to show
     * @param paragraph if true, the text will be shown in its own paragraph; if false, it will
     *          be shown along with any other text that didn't request a separate paragraph
     */
    public void queueLookText(String text, boolean paragraph) {
        if (text.isEmpty())
            return;
        if (paragraph)
            paragraphBuilder.append('\n').append(text).append('\n');
        else
            commonTextBuilder.append(text).append(' ');
    }

    /** Called by when the user clicks "Wait" */
    public void waitCommand() {
        println(bundles.getPassage("wait-message"));
        nextTurn();
    }

    /**
     * Called to indicate that the given entity's state has changed in such
     * a way that the UI may need to be refreshed. Any such UI updates will
     * be performed not at the time of the method call, but at the end of the turn.
     * @param e entity that has changed
     */
    public void entityChanged(Entity e) {
        changedEntities.add(e);
    }

    private void entityChangedImpl(Entity e) {
        if (e == null)
            return;
        if (e.getContainer() == currentRoom) {
            ui.updateRoomEntity(e.getId(), e.getName());
        } else if (isInInventory(e)) {
            String listname;
            if (isEquipped(e))
                listname = e.getName() + " (e)";
            else
                listname = e.getName();
            ui.updateInventoryEntity(e.getId(), listname);
        }
        if (e == selectedEntity)
            queueEntityUIRefresh();
    }

    /**
     * Called when a room's internal state changes in such a way that the UI
     * may have to be updated to reflect the change. Any such UI updates will
     * be performed not at the time of the method call, but at the end of the turn.
     * @param r room that has changed
     */
    public void roomChanged(Room r) {
        changedRooms.add(r);
    }

    private void roomChangedImpl(Room r) {
        if (r == currentRoom) {
            queueRoomUIRefresh();
        } else {
            // If the changed room is adjacent to the current room, it's possible that
            // the exit label it supplied will have changed as well.
            for (int pos = 0; pos < UIConstants.NUM_EXIT_BUTTONS; pos++)
                if (currentRoom.getExit(pos) == r)
                    ui.setExitLabel(pos, currentRoom.getExitLabel(pos));
        }
    }

    /* Go through our changed object queues and refresh the UI as necessary. We make copies
       of the changed-object sets before iterating over them to avoid potentially infinite
       loops where a UI refresh triggers some cycle of adding the same objects to the sets
       over and over. */
    private void processChangedObjects() {
        if (!changedEntities.isEmpty()) {
            entityProcessingList.addAll(changedEntities);
            changedEntities.clear();
            for (Entity e : entityProcessingList)
                entityChangedImpl(e);
            entityProcessingList.clear();
        }
        if (!changedRooms.isEmpty()) {
            roomProcessingList.addAll(changedRooms);
            changedRooms.clear();
            for (Room r : roomProcessingList)
                roomChangedImpl(r);
            roomProcessingList.clear();
        }
    }

    /** Get the status bar provider responsible for updating the status bar each turn. */
    public StatusBarProvider getStatusBarProvider() {
        return statusBarProvider;
    }

    /** Set the status bar provider responsible for updating the status bar each turn. */
    public void setStatusBarProvider(StatusBarProvider statusBarProvider) {
        this.statusBarProvider = statusBarProvider;
    }

    /** Refresh the UI as necessary. */
    public void refreshUI() {
        processChangedObjects();
        if (roomRefreshNeeded) {
            refreshRoomUI();
            roomRefreshNeeded = false;
        }
        if (entityRefreshNeeded) {
            refreshEntityUI();
            entityRefreshNeeded = false;
        }
        if (inventoryRefreshNeeded) {
            refreshInventoryUI();
            inventoryRefreshNeeded = false;
        }
        if (statusBarProvider != null) {
            for (int labelPos = 0; labelPos < UIConstants.NUM_LABELS; labelPos++)
                ui.setStatusLabel(labelPos, statusBarProvider.getStatusText(labelPos));
        }
    }

    /** Called as one turn is transitioning to the next. */
    void nextTurn() {
        if (!endGameSignalled) {
            handlerManager.fireTurn();
            currentRoom.eachTurn();
            if (lookNeeded) {  // set when we're moving rooms
                lookNeeded = false;
                performLook();
            }
        }
        outputText();  // send any buffered text to the UI
        refreshUI();
        numTurns++;
        if (endGameSignalled) {
            endGameSignalled = false;
            if (ui.showTextDialogImpl("Save Transcript",
                "Do you want to save the transcript before ending the game?",
                "Save", "Don't") == 0)
                ui.doSaveTranscript();
            closeGame();
            ui.noGameLoop();
        }
    }

    /** Called when the user clicks an exit button */
    public void exitSelected(int buttonPosition) {
        Room toRoom = currentRoom.getExit(buttonPosition);
        if (toRoom != null) {
            movePlayer(toRoom);
        } else {
            // This can occur if there is an exit label on a room, but not an exit,
            // for instance if a closed door is in the way.
            println(bundles.getPassage("exit-blocked-message"));
        }
        nextTurn();
    }

    /** Called by when the user clicks an action button (or selects an action
     *  from the combo box when there are many actions) */
    public void entityActionSelected(Action action) {
        boolean actionHandled = false;
        actionChain:
        {
            if (actionHandled = handlerManager.fireGameAction(action, selectedEntity, true))
                break actionChain;
            if (actionHandled = selectedEntity.processAction(action))
                break actionChain;
            if (actionHandled = handlerManager.fireGameAction(action, selectedEntity, false))
                break actionChain;
        }
        if (handlerManager.firePostAction(action, selectedEntity, actionHandled) == false && !actionHandled)
            println(bundles.getPassage("action-not-handled-message"));
        nextTurn();
    }

    /**
     * Called by to indicate that the user selected an entity in the lists.
     * @param id ID of the selected entity, or null if no entity selected
     */
    public void entitySelected(String id) {
        if (id == null)
            selectedEntity = null;
        else
            selectedEntity = entityIdMap.get(id);
        refreshEntityUI();  // we must update immediately!
        if (selectedEntity != null)
            handlerManager.fireEntitySelected(selectedEntity);
    }

    private void putBinding(String name, Object value) {
        Meterman2.template.putBinding(name, value);
        Meterman2.script.putBinding(name, value);
    }

    private void putBindings(Map<String,Object> bindings) {
        Meterman2.template.putBindings(bindings);
        Meterman2.script.putBindings(bindings);
    }

    /**
     * Print text to the main UI text area. Games should call this method instead of going directly
     * to Meterman2.ui so that we can process or buffer the text, if the situation warrants.
     * @param text text to print
     */
    public void print(String text) {
        outputBuilder.append(text);
    }

    /**
     * Print text to the main UI text area, followed by a newline.
     * @param text text to print
     */
    public void println(String text) {
        outputBuilder.append(text).append('\n');
    }

    /** Prints the text of a text-source. */
    public void print(TextSource source) {
        print(source.getText());
    }

    /** Prints the text of a text-source, followed by a newline. */
    public void println(TextSource source) {
        println(source.getText());
    }

    /** If, on this turn, there is any text queued for output, inserts a blank line so that the
     *  next thing printed will appear as a new paragraph. */
    public void newPar() {
        final int len = outputBuilder.length();
        if (len != 0) {
            if (outputBuilder.charAt(len-1) != '\n')
                outputBuilder.append('\n');
            outputBuilder.append('\n');
        }
    }

    /**
     * Sends any text queued in {@code outputBuilder} to the UI, after allowing
     * {@code OutputTextProcessor}S a chance to modify it.
     */
    public void outputText() {
        final int len = outputBuilder.length();
        if (len != 0) {
            handlerManager.fireOutputTextReady(outputBuilder);
            if (outputBuilder.charAt(len-1) != '\n')  // Ensure the text ends with a newline
                outputBuilder.append('\n');
            final String outputText = outputBuilder.toString();
            outputBuilder.setLength(0);

            ui.appendText(outputSeparator, false);
            ui.appendMarkupText(outputText);
            transcript.append(outputSeparator);
            transcript.append(Markup.plainTextMarkup(outputText));
        }
    }

    /** Return the text of the current game transcript. */
    String getTranscript() {
        return transcript.toString();
    }

    /**
     * Signals that the game should end at the end of the current turn.
     * <p/>
     * At that point, we prompt the user to save the transcript, if applicable, and
     * then close the current game and go to the "new/load/quit" loop as seen on startup.
     */
    public void endGame() {
        endGameSignalled = true;
    }

    /** Called by the when it's time to load a saved game. */
    void loadGameState(InputStream in) {
        ui.showWaitDialog("Loading game...");
        GameState state;
        try {
            state = Meterman2.persistence.loadGameState(in);
        } catch (Exception ex) {
            ui.hideWaitDialog();
            ui.showTextDialogImpl("Load Error", "Invalid save game file!", "Ayaa");
            return;
        }
        if (!Meterman2.gamesList.gameExists(state.gameName)) {
            ui.hideWaitDialog();
            ui.showTextDialogImpl("Invalid Game", "The game in the save file doesn't exist anymore!", "Ayaa");
            return;
        }
        final String gameVersion = Meterman2.gamesList.getGameVersion(state.gameName);
        if (state.engineVersion != Meterman2.VERSION || !state.gameVersion.equals(gameVersion)) {
            ui.hideWaitDialog();
            if (ui.showTextDialogImpl("Version Mismatch",
                "The version of the program or game used to save the file is different from the current version.\n\n" +
                "Current versions: Engine (" + Meterman2.VERSION + ") Game (" + gameVersion + ")\n" +
                "  Saved versions: Engine (" + state.engineVersion + ") Game (" + state.gameVersion + ")\n\n" +
                "This may cause internal chaos if loaded: do you want to try anyway?", "Try Anyway", "Abort") != 0)
                return;
            else
                ui.showWaitDialog("Carrying on...");   // re-show the wait message and carry on
        }
        final Game g = Meterman2.gamesList.createGame(state.gameName);
        if (g == null) {
            ui.hideWaitDialog();
            ui.showTextDialogImpl("Game Error", "The game in the save file could not be created!", "Ayoo");
        } else {
            loadGame(g, state);  // in turn calls restoreGameState() below, and hides the wait dialog
            ui.appendText("\n   ------- Game Loaded -------\n\n", true);
        }
    }
    
    // Patch up the properties of entities, rooms, and the player from saved values in game-state.
    private void restoreGameObjectProperties(final Map<String,Entity> entityIdMap, final Map<String,Room> roomIdMap,
                                             final Player player, final GameState state) 
    {
        AttributeSetPermuter attrPermuter =
            new AttributeSetPermuter(Arrays.asList(state.attributeNames), Meterman2.attributes.getAttributeNames());

        // fix up the state of all entities in the entityIdMap from state.entityState
        for (Map.Entry<String,Entity> entry : entityIdMap.entrySet()) {
            String id = entry.getKey();
            Entity e = entry.getValue();
            GameState.EntityState entityState = state.entityStateMap.get(id);
            if (entityState == null)
                continue;
            e.setName(entityState.name);
            e.setIndefiniteArticle(entityState.indefiniteArticle);
            e.getAttributes().setTo(entityState.attributes);
            attrPermuter.permuteAttributeSet(e.getAttributes());
            if (e instanceof EntityContainer)
                populateContainer((EntityContainer) e, entityState.contentIds);
            e.restoreState(entityState.stateObj);
        }

        // fix up the state of all rooms in the roomIdMap from state.roomState
        for (Map.Entry<String,Room> entry : roomIdMap.entrySet()) {
            String id = entry.getKey();
            Room r = entry.getValue();
            GameState.RoomState roomState = state.roomStateMap.get(id);
            if (roomState == null)
                continue;
            r.setName(roomState.name);
            r.setExitName(roomState.exitName);
            r.getAttributes().setTo(roomState.attributes);
            attrPermuter.permuteAttributeSet(r.getAttributes());
            for (int i = 0; i < UIConstants.NUM_EXIT_BUTTONS; i++) {
                final String exitId = roomState.exitRoomIds[i];
                if (exitId != null)
                    r.setExit(i, roomIdMap.get(exitId));
                r.setExitLabel(i, roomState.exitLabels[i]);
            }
            populateContainer(r, roomState.contentIds);
            r.restoreState(roomState.stateObj);
        }
        // restore custom object properties
        objectProps.restoreFromIdMaps(state.entityIdPropertyMap, state.roomIdPropertyMap, entityIdMap, roomIdMap);

        // restore player state from state.playerState
        final GameState.PlayerState playerState = state.playerState;
        populateContainer(player, playerState.inventoryEntityIds);
        player.clearEquippedEntities();
        final List<Entity> inventoryEntities = player.getEntities();
        final String[] equippedEntityIds = playerState.equippedEntityIds;
        for (Entity e : inventoryEntities) {
            if (ArrayUtils.contains(equippedEntityIds, e.getId()))
                player.equipEntity(e);
        }
    }

    private void populateContainer(EntityContainer c, String[] contentIds) {
        c.clearEntities();
        if (contentIds != null) {
            for (String id : contentIds) {
                final Entity e = entityIdMap.get(id);
                if (e != null) {
                    c.addEntity(e);
                    e.setContainer(c);
                }
            }
        }
    }

    // restore game handlers from state.gameHandlers
    private void restoreHandlers(final GameState state, final Game g) {
        for (String id : state.gameHandlers.get("gameActionListeners"))
            addGameActionListener((GameActionListener) g.getEventHandler(id));
        for (String id : state.gameHandlers.get("playerMovementListeners"))
            addPlayerMovementListener((PlayerMovementListener) g.getEventHandler(id));
        for (String id : state.gameHandlers.get("turnListeners"))
            addTurnListener((TurnListener) g.getEventHandler(id));
        for (String id : state.gameHandlers.get("entityActionsProcessors"))
            addEntityActionsProcessor((EntityActionsProcessor) g.getEventHandler(id));
        for (String id : state.gameHandlers.get("entitySelectionListeners"))
            addEntitySelectionListener((EntitySelectionListener) g.getEventHandler(id));
        for (String id : state.gameHandlers.get("outputTextProcessors"))
            addOutputTextProcessor((OutputTextProcessor) g.getEventHandler(id));
        for (String id : state.gameHandlers.get("lookListeners"))
            addLookListener((LookListener) g.getEventHandler(id));
    }

    /** Called by the UI when it's time to save a game. */
    void saveGameState(OutputStream out) {
        ui.showWaitDialog("Saving game...");
        GameState state = new GameState();
        state.engineVersion = Meterman2.VERSION;
        state.gameName = game.getName();
        state.gameVersion = Meterman2.gamesList.getGameVersion(state.gameName);
        state.gameStateMap = new HashMap<>(gameStateMap);
        state.attributeNames = Meterman2.attributes.getAttributeNames().toArray(new String[0]);
        state.entityStateMap = Utils.createSizedHashMap(entityIdMap);
        for (Map.Entry<String,Entity> entry : entityIdMap.entrySet()) {
            String id = entry.getKey();
            Entity entity = entry.getValue();
            GameState.EntityState entityState = new GameState.EntityState();
            entityState.name = entity.getNameProperty();
            entityState.indefiniteArticle = entity.getIndefiniteArticle();
            entityState.attributes = entity.getAttributes();
            if (entity instanceof EntityContainer)
                entityState.contentIds = getContainerContentIds((EntityContainer) entity);
            entityState.stateObj = entity.getState();
            state.entityStateMap.put(id, entityState);
        }
        state.roomStateMap = Utils.createSizedHashMap(roomIdMap);
        for (Map.Entry<String,Room> entry : roomIdMap.entrySet()) {
            String id = entry.getKey();
            Room room = entry.getValue();
            GameState.RoomState roomState = new GameState.RoomState();
            roomState.name = room.getNameProperty();
            roomState.exitName = room.getExitNameProperty();
            roomState.attributes = room.getAttributes();
            roomState.exitRoomIds = new String[UIConstants.NUM_EXIT_BUTTONS];
            roomState.exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
            for (int position = 0; position < UIConstants.NUM_EXIT_BUTTONS; position++) {
                final Room exit = room.getExitProperty(position);
                if (exit != null)
                    roomState.exitRoomIds[position] = exit.getId();
                roomState.exitLabels[position] = room.getExitLabelProperty(position);
            }
            roomState.contentIds = getContainerContentIds(room);
            roomState.stateObj = room.getState();
            state.roomStateMap.put(id, roomState);
        }
        final GameState.PlayerState playerState = new GameState.PlayerState();
        playerState.inventoryEntityIds = getContainerContentIds(player);
        playerState.equippedEntityIds = getEntityIds(player.getEquippedEntities());
        state.playerState = playerState;

        state.gameHandlers = new HashMap<>(10, 0.75f);
        state.handlerStateMap = new HashMap<>();
        for (Map.Entry<String, List<? extends GameEventHandler>>
                entry : handlerManager.getEventHandlerMap().entrySet()) {
            String listName = entry.getKey();
            List<? extends GameEventHandler> handlerList = entry.getValue();
            final ArrayList<String> handlerIds = new ArrayList<>(handlerList.size());
            for (GameEventHandler handler : handlerList) {
                final String handlerId = handler.getHandlerId();
                if (handlerId != null && !handlerId.startsWith("#")) {
                    handlerIds.add(handlerId);
                    final Object handlerState = handler.getHandlerState();
                    if (handlerState != null)
                        state.handlerStateMap.put(handlerId, handlerState);
                }
            }
            state.gameHandlers.put(listName, handlerIds.toArray(new String[0]));
        }
        state.entityIdPropertyMap = Utils.createSizedHashMap(objectProps.getEntityPropertyMap());
        state.roomIdPropertyMap = Utils.createSizedHashMap(objectProps.getRoomPropertyMap());
        objectProps.saveToIdMaps(state.entityIdPropertyMap, state.roomIdPropertyMap);
        state.currentRoomId = currentRoom.getId();
        state.numTurns = numTurns;
        Meterman2.persistence.saveGameState(state, out);
        ui.hideWaitDialog();
        ui.appendText("\n   ------- Game Saved -------\n\n", true);
    }

    // Returns the content IDs of a container's contents, or null if no contents.
    private String[] getContainerContentIds(EntityContainer c) {
        return getEntityIds(c.getEntities());
    }

    private String[] getEntityIds(Collection<Entity> c) {
        String[] entityIds = null;
        if (!c.isEmpty()) {
            entityIds = new String[c.size()];
            int idx = 0;
            for (Entity e : c)
                entityIds[idx++] = e.getId();
        }
        return entityIds;
    }

    //region -- Delegate event handler registration to the gameHandlerManager --
    public void addGameActionListener(GameActionListener l) {handlerManager.addGameActionListener(l);}
    public void removeGameActionListener(GameActionListener l) {handlerManager.removeGameActionListener(l);}

    public void addPlayerMovementListener(PlayerMovementListener l) {handlerManager.addPlayerMovementListener(l);}
    public void removePlayerMovementListener(PlayerMovementListener l) {handlerManager.removePlayerMovementListener(l);}

    public void addTurnListener(TurnListener l) {handlerManager.addTurnListener(l);}
    public void removeTurnListener(TurnListener l) {handlerManager.removeTurnListener(l);}

    public void addEntityActionsProcessor(EntityActionsProcessor l) {handlerManager.addEntityActionsProcessor(l);}
    public void removeEntityActionsProcessor(EntityActionsProcessor l) {handlerManager.removeEntityActionsProcessor(l);}

    public void addEntitySelectionListener(EntitySelectionListener l) {handlerManager.addEntitySelectionListener(l);}
    public void removeEntitySelectionListener(EntitySelectionListener l) {handlerManager.removeEntitySelectionListener(l);}

    public void addOutputTextProcessor(OutputTextProcessor p) {handlerManager.addOutputTextProcessor(p);}
    public void removeOutputTextProcessor(OutputTextProcessor p) {handlerManager.removeOutputTextProcessor(p);}

    public void addLookListener(LookListener l) {handlerManager.addLookListener(l);}
    public void removeLookListener(LookListener l) {handlerManager.removeLookListener(l);}
    //endregion

    /**
     * Called when an action on the selected entity is performed that uses an additional object (ex. putting
     * an item in a continer).
     * @param action action
     * @param object the additional object
     * @return true if the action was blocked; false to allow the action to continue.
     */
    public boolean objectAction(Action action, Entity object) {
        if (selectedEntity == null)
            return false;
        else if (object.objectAction(action, selectedEntity) == true)
            return true;
        else
            return handlerManager.fireObjectAction(object, action, selectedEntity);
    }
}
