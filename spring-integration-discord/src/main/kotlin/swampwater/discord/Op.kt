package swampwater.discord

enum class Op {
    Dispatch,
    Heartbeat,
    Identity,
    StatusUpdate,
    VoiceStateUpdate,
    VoiceServerPing,
    Resume,
    Reconnect,
    RequestGuildMembers,
    InvalidSession,
    Hello,
    HeartbeatAck
}
