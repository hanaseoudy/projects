package multiplayer.packet;

public enum PacketType {

    // Room Actions
    NAME,
    LEAVE_ROOM,
    UPDATE_ROOM,

    // Client Game Actions
    SELECT_CARD,
    SELECT_MARBLE,
    DESELECT_MARBLE,
    SPLIT_DISTANCE,
    PLAY,

    // Server/Host Game Actions
    UPDATE_GAME,
    UPDATE_FIREPIT,

    // Actions Results
    ERROR,
    SUCCESS,

    // Utils
    CHAT,
    HEARTBEAT
}
