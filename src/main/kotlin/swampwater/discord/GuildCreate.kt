package swampwater.discord

import java.time.OffsetDateTime

data class GuildCreate(
        val id: String,
        val name: String,
        val icon: String?,
        val splash: String?,
        val ownerId: String,
        val region: String,
        val afkChannelId: String?,
        val afkTimeout: Int,
        val embedEnabled: Boolean,
        val embedChannelId: String?,
        val verificationLevel: Int,
        val defaultMessageNotifications: Int,
        val roles: List<Role>,
        val emojis: List<Emoji>,
        val features: List<Any>,
        val mfaLevel: Int,
        val joinedAt: OffsetDateTime,
        val large: Boolean,
        val unavailable: Boolean,
        val memberCount: Int,
        val voiceStates: List<VoiceState>,
        val members: List<Member>,
        val channels: List<GuildChannel>,
        val presences: List<Any>,
        val applicationId: String?
)
