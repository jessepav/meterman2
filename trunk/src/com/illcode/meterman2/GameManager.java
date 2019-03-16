package com.illcode.meterman2;

import com.illcode.meterman2.event.*;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.state.GameState;
import com.illcode.meterman2.text.TextSource;
import com.illcode.meterman2.ui.UIConstants;
import com.illcode.meterman2.MMActions.Action;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.illcode.meterman2.model.GameUtils.hasAttr;
import static com.illcode.meterman2.model.GameUtils.setAttr;
import static com.illcode.meterman2.Meterman2.ui;
import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.SystemAttributes.EQUIPPABLE;
import static com.illcode.meterman2.SystemAttributes.VISITED;

public final class GameManager
{
    public static final String ACTION_NOT_HANDLED_PASSAGE_ID = "action-not-handled-message";
    public static final String WAIT_PASSAGE_ID = "wait-message";

    private Game game;  // The game we're currently playing
    private Player player;
    private Map<String,Object> gameStateMap;
    private Map<String,Entity> entityIdMap;
    private Map<String,Room> roomIdMap;

    private Room currentRoom;
    private Entity selectedEntity;  // currently selected entity, or null if none
    private int numTurns;

    private EventHandlerManager handlerManager;

    // To be used in composing text before sending it off to the UI.
    private StringBuilder outputBuilder;

    private StringBuilder commonTextBuilder, paragraphBuilder; // See queueLookText()
    private List<Action> actions; // Used for composing UI actions - reuse same list to avoid allocation
    private boolean alwaysLook; // see setAlwaysLook()

    GameManager() {
        handlerManager = new EventHandlerManager();

        outputBuilder = new StringBuilder(2048);
        commonTextBuilder = new StringBuilder(1024);
        paragraphBuilder = new StringBuilder(1024);

        actions = new ArrayList<>(16);
    }


    void dispose() {
        closeGame();

        player = null;
        handlerManager = null;
        outputBuilder = null;
        commonTextBuilder = null;
        paragraphBuilder = null;

        actions = null;
    }

    /**
     * Start a new game, and perform an initial "Look" command.
     * @param game game to start
     */
    void newGame(Game game) {
        closeGame();
        this.game = game;
        String gameName = game.getName();
        ui.showWaitDialog("Starting " + gameName + "...");
        ui.setGameName(gameName);
        Meterman2.assets.setGameAssetsPath(Meterman2.gamesList.getGameAssetsPath(gameName));
        ui.clearText();
        game.init();
        gameStateMap = game.getGameStateMap();
        game.constructWorld();
        entityIdMap = game.getEntityIdMap();
        roomIdMap = game.getRoomIdMap();
        game.setInitialWorldState();
        numTurns = 0;
        player = game.getPlayer();
        currentRoom = game.getStartingRoom();
        game.registerInitialGameHandlers();

        putBindings(gameStateMap);
        putBinding("room", currentRoom);

        refreshRoomUI();
        refreshInventoryUI();
        entitySelected(null);
        ui.setFrameImage(UIConstants.DEFAULT_FRAME_IMAGE);
        ui.hideWaitDialog();
        game.start(true);
        currentRoom.entered(null);
        lookCommand();
        setAttr(currentRoom, VISITED);
    }

    void loadGame(GameState gameState) {
        closeGame();
        game = Meterman2.gamesList.createGame(gameState.gameName);
        String gameName = game.getName();
        ui.setGameName(gameName);
        Meterman2.assets.setGameAssetsPath(Meterman2.gamesList.getGameAssetsPath(gameName));
        game.init();
        gameStateMap = gameState.gameStateMap;
        game.setGameStateMap(gameStateMap);
        game.constructWorld();
        entityIdMap = game.getEntityIdMap();
        roomIdMap = game.getRoomIdMap();
        player = new Player();

        // fix up the state of all entities in the entityIdMap from state.entityState
        for (Map.Entry<String,Entity> entry : entityIdMap.entrySet()) {
            String entityId = entry.getKey();
            Entity entity = entry.getValue();
            GameState.EntityState entityState = gameState.entityStateMap.get(entityId);
            entity.setName(entityState.name);
            entity.setIndefiniteArticle(entityState.indefiniteArticle);
            EntityContainer container = null;
            if (entityState.containerId != null) {
                switch (entityState.containerType) {
                case EntityContainer.CONTAINER_ENTITY:
                    Entity e = entityIdMap.get(entityState.containerId);
                    if (e instanceof EntityContainer)  // just in case
                        container = (EntityContainer) e;
                    break;
                case EntityContainer.CONTAINER_ROOM:
                    container = roomIdMap.get(entityState.containerId);
                    break;
                case EntityContainer.CONTAINER_PLAYER:
                    container = player;
                    break;
                }
            }
            entity.setContainer(container);
            if (container != null)
                container.addEntity(entity);
            entity.getAttributes().setTo(entityState.attributes);
            if (entityState.stateObj != null)
                entity.restoreState(entityState.stateObj);
        }

        // fix up the state of all rooms in the roomIdMap from state.roomState
        for (Map.Entry<String,Room> entry : roomIdMap.entrySet()) {
            String roomId = entry.getKey();
            Room room = entry.getValue();
            GameState.RoomState roomState = gameState.roomStateMap.get(roomId);

            room.setName(roomState.name);
            room.setExitName(roomState.exitName);
            room.getAttributes().setTo(roomState.attributes);
            for (int i = 0; i < UIConstants.NUM_EXIT_BUTTONS; i++) {
                final String id = roomState.exitRoomIds[i];
                if (id != null)
                    room.setExit(i, roomIdMap.get(id));
                room.setExitLabel(i, roomState.exitLabels[i]);
            }
            if (roomState.stateObj != null)
                room.restoreState(roomState.stateObj);
        }

        // restore player state from state.playerState
        for (String entityId : gameState.playerState.equippedEntityIds)
            player.equipEntity(entityIdMap.get(entityId));

        // restore game handlers from state.gameHandlers
        for (String id : gameState.gameHandlers.get("gameActionListeners"))
            addGameActionListener((GameActionListener) game.getEventHandler(id));
        for (String id : gameState.gameHandlers.get("playerMovementListeners"))
            addPlayerMovementListener((PlayerMovementListener) game.getEventHandler(id));
        for (String id : gameState.gameHandlers.get("turnListeners"))
            addTurnListener((TurnListener) game.getEventHandler(id));
        for (String id : gameState.gameHandlers.get("entityActionsProcessors"))
            addEntityActionsProcessor((EntityActionsProcessor) game.getEventHandler(id));
        for (String id : gameState.gameHandlers.get("entitySelectionListeners"))
            addEntitySelectionListener((EntitySelectionListener) game.getEventHandler(id));
        for (String id : gameState.gameHandlers.get("outputTextProcessors"))
            addOutputTextProcessor((OutputTextProcessor) game.getEventHandler(id));

        currentRoom = roomIdMap.get(gameState.currentRoomId);
        numTurns = gameState.numTurns;

        putBindings(gameStateMap);
        putBinding("room", currentRoom);

        refreshRoomUI();
        refreshInventoryUI();
        entitySelected(null);
        ui.setFrameImage(UIConstants.DEFAULT_FRAME_IMAGE);
        ui.hideWaitDialog();
        game.start(false);
    }

    private void closeGame() {
        handlerManager.clearListenerLists();
        player = null;
        currentRoom = null;
        gameStateMap = null;
        entityIdMap = null;
        roomIdMap = null;

        if (game != null) {
            game.dispose();
            game = null;
        }
        Meterman2.ui.clearImages();
        Meterman2.sound.clearAudio();
        Meterman2.script.clearGameBindings();
        Meterman2.template.clearBindings();
        Meterman2.template.clearGameTemplates();
        Meterman2.template.clearTemplateCache();
        Meterman2.attributes.clearGameAttributes();
        Meterman2.actions.clearGameActions();
        Meterman2.bundles.clearGameBundles();
        Meterman2.assets.setGameAssetsPath(null);
        ui.setGameName(null);
        ui.clearStatusLabels();
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
        for (Entity e : GameUtils.getEntitiesRecursive(fromRoom))
            e.exitingScope();
        currentRoom = toRoom;  // we've moved!
        putBinding("room", currentRoom);
        toRoom.entered(fromRoom);
        for (Entity e : GameUtils.getEntitiesRecursive(toRoom))
            e.enterScope();
        handlerManager.firePlayerMovement(fromRoom, toRoom, false);
        ui.clearEntitySelection();  // this in turn will call entitySelected(null) if needed
        if (alwaysLook || !hasAttr(toRoom, SystemAttributes.VISITED))
            performLook();
        setAttr(toRoom, SystemAttributes.VISITED);
        refreshRoomUI();
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
        boolean uiRefreshNeeded = false;
        if (GameUtils.isParentContainer(player, fromContainer)) {
            if (!GameUtils.isParentContainer(player, toContainer)) {
                player.unequipEntity(e);
                e.dropped();
            }
            uiRefreshNeeded = true;
        }
        final Room fromRoom = GameUtils.getRoom(fromContainer);  // keep track of rooms for scope
        final Room toRoom = GameUtils.getRoom(toContainer);
        if (fromRoom != toRoom && fromRoom == currentRoom)
            e.exitingScope();
        // Now move the entity.
        if (fromContainer != null)
            fromContainer.removeEntity(e);
        e.setContainer(toContainer);
        if (toContainer != null)
            toContainer.addEntity(e);
        // done moving!
        if (fromRoom != toRoom && toRoom == currentRoom)
            e.enterScope();
        if (fromContainer == currentRoom || toContainer == currentRoom)
            refreshRoomUI();
        if (e == selectedEntity)
            entitySelected(null);
        if (GameUtils.isParentContainer(player, toContainer)) {
            if (!GameUtils.isParentContainer(player, fromContainer))
                e.taken();
            uiRefreshNeeded = true;
        }
        if (uiRefreshNeeded)
            refreshInventoryUI();
    }

    /** Returns true if the given entity is in the player inventory. */
    public boolean isInInventory(Entity e) {
        return e.getContainer() == player;
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
        final Set<Entity> equippedEntities = player.getEquippedEntities();
        if (equip) {
            if (hasAttr(e, EQUIPPABLE) && isInInventory(e) && !equippedEntities.contains(e)) {
                equippedEntities.add(e);
                refreshInventoryUI();
                return true;
            }
        } else {
            if (equippedEntities.remove(e)) {
                refreshInventoryUI();
                return true;
            }
        }
        return false;
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


    private void refreshRoomUI() {
        Room r = getCurrentRoom();
        ui.setRoomName(r.getName());
        for (int pos = 0; pos < UIConstants.NUM_EXIT_BUTTONS; pos++)
            ui.setExitLabel(pos, r.getExitLabel(pos));
        ui.clearRoomEntities();
        for (Entity e : r.getEntities())
            if (!e.getAttributes().get(SystemAttributes.CONCEALED))
                ui.addRoomEntity(e.getId(), e.getName());
    }

    /**
     * Called when the player inventory changes in such a way that the UI needs to be refreshed.
     */
    private void refreshInventoryUI() {
        Entity savedSE = selectedEntity;
        List<Entity> inventory = player.getEntities();
        Set<Entity> equippedItems = player.getEquippedEntities();
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
        if (isInInventory(savedSE))
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
        outputBuilder.append(currentRoom.getDescription());
        outputBuilder.append("\n");
        for (Entity e : currentRoom.getEntities())
            e.lookInRoom();
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
        if (paragraph)
            paragraphBuilder.append('\n').append(text).append('\n');
        else
            commonTextBuilder.append(text).append(' ');
    }

    /** Called by when the user clicks "Wait" */
    public void waitCommand() {
        println(bundles.getPassage(WAIT_PASSAGE_ID));
        nextTurn();
    }

    /** Called as one turn is transitioning to the next. */
    private void nextTurn() {
        handlerManager.fireTurn();
        outputText();  // send any buffered text to the UI
        numTurns++;
    }

    /** Called when the user clicks an exit button */
    public void exitSelected(int buttonPosition) {
        Room toRoom = currentRoom.getExit(buttonPosition);
        if (toRoom != null)
            movePlayer(toRoom);
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
            println(bundles.getPassage(ACTION_NOT_HANDLED_PASSAGE_ID));
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
        refreshEntityUI();
        if (selectedEntity != null)
            handlerManager.fireEntitySelected(selectedEntity);
    }

    /**
     * Called to indicate that the given entity's state has changed in such
     * a way that the UI may need to be refreshed.
     * @param e entity that has changed
     */
    public void entityChanged(Entity e) {
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
            refreshEntityUI();
    }

    /**
     * Called when a room's internal state changes in such a way that the UI
     * may have to be updated to reflect the change.
     * @param r room that has changed
     */
    public void roomChanged(Room r) {
        if (r == currentRoom) {
            refreshRoomUI();
        } else {
            // If the changed room is adjacent to the current room, it's possible that
            // the exit label it supplied will have changed as well.
            for (int pos = 0; pos < UIConstants.NUM_EXIT_BUTTONS; pos++)
                if (currentRoom.getExit(pos) == r)
                    ui.setExitLabel(pos, currentRoom.getExitLabel(pos));
        }
    }

    private void putBinding(String name, Object value) {
        Meterman2.template.putBinding(name, value);
        Meterman2.script.putBinding(name, value);
    }

    private void putBindings(Map<String,Object> bindings) {
        Meterman2.template.putBindings(bindings);
        Meterman2.script.putGameBindings(bindings);
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

    /**
     * Sends any text queued in {@code outputBuilder} to the UI, after allowing
     * {@code OutputTextProcessor}S a chance to modify it.
     */
    private void outputText() {
        if (outputBuilder.length() != 0) {
            handlerManager.fireOutputTextReady(outputBuilder);
            ui.appendText(outputBuilder.toString());
            outputBuilder.setLength(0);
        }
    }

    /** Called by the when it's time to load a saved game. */
    void loadGameState(InputStream in) {
        ui.showWaitDialog("Loading game...");
        loadGame(Meterman2.persistence.loadGameState(in));
        ui.appendText("\n------- Game Loaded -------\n\n");
    }

    /** Called by the UI when it's time to save a game. */
    void saveGameState(OutputStream out) {
        ui.showWaitDialog("Saving game...");
        GameState state = new GameState();
        state.gameName = game.getName();
        state.gameStateMap = new HashMap<>(gameStateMap);
        state.entityStateMap = new HashMap<>((int) (entityIdMap.size() * 1.4f), 0.75f);
        for (Map.Entry<String,Entity> entry : entityIdMap.entrySet()) {
            String id = entry.getKey();
            Entity entity = entry.getValue();
            GameState.EntityState entityState = new GameState.EntityState();
            entityState.name = entity.getName();
            entityState.indefiniteArticle = entity.getIndefiniteArticle();
            final EntityContainer container = entity.getContainer();
            if (container != null) {
                entityState.containerId = container.getContainerId();
                entityState.containerType = container.getContainerType();
            }
            entityState.attributes = entity.getAttributes();
            entityState.stateObj = entity.getState();
            state.entityStateMap.put(id, entityState);
        }
        state.roomStateMap = new HashMap<>((int) (roomIdMap.size() * 1.4f), 0.75f);
        for (Map.Entry<String,Room> entry : roomIdMap.entrySet()) {
            String id = entry.getKey();
            Room room = entry.getValue();
            GameState.RoomState roomState = new GameState.RoomState();
            roomState.name = room.getName();
            roomState.exitName = room.getExitName();
            roomState.exitRoomIds = new String[UIConstants.NUM_EXIT_BUTTONS];
            roomState.exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
            for (int position = 0; position < UIConstants.NUM_EXIT_BUTTONS; position++) {
                final Room exit = room.getExit(position);
                if (exit != null)
                    roomState.exitRoomIds[position] = exit.getId();
                roomState.exitLabels[position] = room.getExitLabel(position);
            }
            roomState.stateObj = room.getState();
            state.roomStateMap.put(id, roomState);
        }
        state.playerState = new GameState.PlayerState();
        state.playerState.equippedEntityIds = new String[player.getEquippedEntities().size()];
        player.getEquippedEntities().toArray(state.playerState.equippedEntityIds);
        state.gameHandlers = new HashMap<>(10, 0.75f);
        for (Map.Entry<String, List<? extends GameEventHandler>>
                entry : handlerManager.getEventHandlerMap().entrySet()) {
            String listName = entry.getKey();
            List<? extends GameEventHandler> handlerList = entry.getValue();
            String[] handlerIds = new String[handlerList.size()];
            int idx = 0;
            for (GameEventHandler handler : handlerList)
                handlerIds[idx++] = handler.getId();
            state.gameHandlers.put(listName, handlerIds);
        }
        state.currentRoomId = currentRoom.getId();
        state.numTurns = numTurns;
        Meterman2.persistence.saveGameState(state, out);
        ui.hideWaitDialog();
        ui.appendText("\n------- Game Saved -------\n\n");
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
    //endregion

}
