package org.discoursey.android.data.api.models

import com.google.gson.annotations.SerializedName

data class TopicListResponse(
    @SerializedName("users") val users: List<User> = emptyList(),
    @SerializedName("topic_list") val topicList: TopicList? = null
)

data class TopicList(
    @SerializedName("can_create_topic") val canCreateTopic: Boolean = false,
    @SerializedName("more_topics_url") val moreTopicsUrl: String? = null,
    @SerializedName("per_page") val perPage: Int = 30,
    @SerializedName("topics") val topics: List<Topic> = emptyList()
)

data class Topic(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("fancy_title") val fancyTitle: String? = null,
    @SerializedName("slug") val slug: String = "",
    @SerializedName("posts_count") val postsCount: Int = 1,
    @SerializedName("reply_count") val replyCount: Int = 0,
    @SerializedName("highest_post_number") val highestPostNumber: Int = 1,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("last_posted_at") val lastPostedAt: String = "",
    @SerializedName("bumped_at") val bumpedAt: String? = null,
    @SerializedName("unseen") val unseen: Boolean = false,
    @SerializedName("pinned") val pinned: Boolean = false,
    @SerializedName("unpinned") val unpinned: Boolean? = false,
    @SerializedName("visible") val visible: Boolean = true,
    @SerializedName("closed") val closed: Boolean = false,
    @SerializedName("archived") val archived: Boolean = false,
    @SerializedName("views") val views: Int = 0,
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("has_summary") val hasSummary: Boolean = false,
    @SerializedName("archetype") val archetype: String = "regular",
    @SerializedName("last_poster_username") val lastPosterUsername: String? = null,
    @SerializedName("category_id") val categoryId: Long? = null,
    @SerializedName("pinned_globally") val pinnedGlobally: Boolean = false,
    @SerializedName("posters") val posters: List<TopicPoster> = emptyList(),
    @SerializedName("excerpt") val excerpt: String? = null,
    // Transient helper fields for UI
    var resolvedLastPosterUser: User? = null,
    var categoryName: String? = null,
    var categoryColor: String? = null
)

data class TopicPoster(
    @SerializedName("extras") val extras: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("user_id") val userId: Long = 0,
    @SerializedName("primary_group_id") val primaryGroupId: Long? = null
)

data class User(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_template") val avatarTemplate: String = "",
    @SerializedName("title") val title: String? = null
) {
    fun getAvatarUrl(baseUrl: String, size: Int = 120): String {
        val cleanBase = baseUrl.trimEnd('/')
        val template = if (avatarTemplate.startsWith("http://") || avatarTemplate.startsWith("https://")) {
            avatarTemplate
        } else {
            "$cleanBase$avatarTemplate"
        }
        return template.replace("{size}", size.toString())
    }

    val displayName: String
        get() = if (!name.isNullOrBlank()) name else username

    fun getDisplayName(preference: String = "AUTO", forumAllowsNames: Boolean = true): String {
        return when (preference) {
            "USERNAME_FIRST" -> username.ifBlank { name ?: "User" }
            "NAME_FIRST" -> if (!name.isNullOrBlank()) name else username
            else -> { // AUTO
                if (forumAllowsNames && !name.isNullOrBlank()) name else username
            }
        }
    }

    fun getSecondaryName(preference: String = "AUTO", forumAllowsNames: Boolean = true): String? {
        val primary = getDisplayName(preference, forumAllowsNames)
        return if (primary == username && !name.isNullOrBlank()) {
            name
        } else if (primary == name && username.isNotBlank()) {
            "@$username"
        } else {
            null
        }
    }
}

data class TopicDetailResponse(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("fancy_title") val fancyTitle: String? = null,
    @SerializedName("posts_count") val postsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("views") val views: Int = 0,
    @SerializedName("reply_count") val replyCount: Int = 0,
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("last_posted_at") val lastPostedAt: String = "",
    @SerializedName("visible") val visible: Boolean = true,
    @SerializedName("closed") val closed: Boolean = false,
    @SerializedName("archived") val archived: Boolean = false,
    @SerializedName("category_id") val categoryId: Long? = null,
    @SerializedName("post_stream") val postStream: PostStream? = null,
    @SerializedName("details") val details: TopicDetails? = null
)

data class PostStream(
    @SerializedName("posts") val posts: List<Post> = emptyList(),
    @SerializedName("stream") val stream: List<Long> = emptyList()
)

data class TopicDetails(
    @SerializedName("can_create_post") val canCreatePost: Boolean = true,
    @SerializedName("can_reply_as_new_topic") val canReplyAsNewTopic: Boolean = true,
    @SerializedName("can_flag_topic") val canFlagTopic: Boolean = true,
    @SerializedName("participants") val participants: List<TopicParticipant> = emptyList()
)

data class TopicParticipant(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_template") val avatarTemplate: String = "",
    @SerializedName("post_count") val postCount: Int = 0
)

data class Post(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("username") val username: String = "",
    @SerializedName("avatar_template") val avatarTemplate: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("cooked") val cooked: String = "",
    @SerializedName("post_number") val postNumber: Int = 1,
    @SerializedName("post_type") val postType: Int = 1,
    @SerializedName("updated_at") val updatedAt: String = "",
    @SerializedName("reply_count") val replyCount: Int = 0,
    @SerializedName("reply_to_post_number") val replyToPostNumber: Int? = null,
    @SerializedName("quote_count") val quoteCount: Int = 0,
    @SerializedName("reads") val reads: Int = 0,
    @SerializedName("readers_count") val readersCount: Int = 0,
    @SerializedName("score") val score: Double = 0.0,
    @SerializedName("yours") val yours: Boolean = false,
    @SerializedName("topic_id") val topicId: Long = 0,
    @SerializedName("topic_slug") val topicSlug: String = "",
    @SerializedName("user_title") val userTitle: String? = null,
    @SerializedName("moderator") val moderator: Boolean = false,
    @SerializedName("admin") val admin: Boolean = false,
    @SerializedName("staff") val staff: Boolean = false,
    @SerializedName("user_id") val userId: Long = 0,
    @SerializedName("hidden") val hidden: Boolean = false,
    @SerializedName("trust_level") val trustLevel: Int = 0,
    @SerializedName("actions_summary") val actionsSummary: List<PostActionSummary> = emptyList(),
    @SerializedName("bookmarked") var bookmarked: Boolean = false,
    @SerializedName("reactions") var reactions: List<PostReaction> = emptyList(),
    @SerializedName("current_user_reaction") var currentUserReaction: PostCurrentUserReaction? = null
) {
    fun getDisplayName(preference: String = "AUTO", forumAllowsNames: Boolean = true): String {
        return when (preference) {
            "USERNAME_FIRST" -> username.ifBlank { name ?: "User" }
            "NAME_FIRST" -> if (!name.isNullOrBlank()) name else username
            else -> { // AUTO
                if (forumAllowsNames && !name.isNullOrBlank()) name else username
            }
        }
    }

    fun getSecondaryName(preference: String = "AUTO", forumAllowsNames: Boolean = true): String? {
        val primary = getDisplayName(preference, forumAllowsNames)
        return if (primary == username && !name.isNullOrBlank()) {
            name
        } else if (primary == name && username.isNotBlank()) {
            "@$username"
        } else {
            null
        }
    }

    fun getAvatarUrl(baseUrl: String, size: Int = 120): String {
        val cleanBase = baseUrl.trimEnd('/')
        val template = if (avatarTemplate.startsWith("http://") || avatarTemplate.startsWith("https://")) {
            avatarTemplate
        } else {
            "$cleanBase$avatarTemplate"
        }
        return template.replace("{size}", size.toString())
    }

    val likeCount: Int
        get() = actionsSummary.firstOrNull { it.id == 2 }?.count ?: 0

    val isLiked: Boolean
        get() = (actionsSummary.firstOrNull { it.id == 2 }?.acted ?: false) || currentUserReaction != null
}

data class PostReaction(
    @SerializedName("id") val id: String = "",
    @SerializedName("type") val type: String = "emoji",
    @SerializedName("count") var count: Int = 0
)

data class PostCurrentUserReaction(
    @SerializedName("id") val id: String = "",
    @SerializedName("can_undo") val canUndo: Boolean = true
)

data class PostActionSummary(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("count") var count: Int = 0,
    @SerializedName("acted") var acted: Boolean = false,
    @SerializedName("can_act") val canAct: Boolean = true
)

data class SiteResponse(
    @SerializedName("default_archetype") val defaultArchetype: String = "regular",
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("enable_names") val enableNames: Boolean = true,
    @SerializedName("categories") val categories: List<Category> = emptyList()
)

data class Category(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("color") val color: String = "652D90",
    @SerializedName("text_color") val textColor: String = "FFFFFF",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("topic_count") val topicCount: Int = 0,
    @SerializedName("post_count") val postCount: Int = 0,
    @SerializedName("description") val description: String? = null,
    @SerializedName("description_text") val descriptionText: String? = null
)

data class NotificationResponse(
    @SerializedName("notifications") val notifications: List<NotificationItem> = emptyList(),
    @SerializedName("total_rows_notifications") val totalRows: Int = 0,
    @SerializedName("seen_notification_id") val seenNotificationId: Long = 0
)

data class NotificationItem(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("user_id") val userId: Long = 0,
    @SerializedName("notification_type") val notificationType: Int = 0,
    @SerializedName("read") var read: Boolean = false,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("post_number") val postNumber: Int? = null,
    @SerializedName("topic_id") val topicId: Long? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("data") val data: NotificationData? = null
)

data class NotificationData(
    @SerializedName("badge_id") val badgeId: Long? = null,
    @SerializedName("badge_name") val badgeName: String? = null,
    @SerializedName("badge_slug") val badgeSlug: String? = null,
    @SerializedName("badge_title") val badgeTitle: Boolean? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("display_username") val displayUsername: String? = null,
    @SerializedName("topic_title") val topicTitle: String? = null,
    @SerializedName("original_post_id") val originalPostId: Long? = null,
    @SerializedName("original_post_type") val originalPostType: Int? = null,
    @SerializedName("original_username") val originalUsername: String? = null,
    @SerializedName("count") val count: Int? = null
)

data class UserProfileResponse(
    @SerializedName("user") val user: UserProfile? = null
)

data class UserProfile(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_template") val avatarTemplate: String = "",
    @SerializedName("bio_raw") val bioRaw: String? = null,
    @SerializedName("website") val website: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("last_posted_at") val lastPostedAt: String? = null,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
    @SerializedName("badge_count") val badgeCount: Int = 0,
    @SerializedName("time_read") val timeRead: Long = 0,
    @SerializedName("recent_time_read") val recentTimeRead: Long = 0,
    @SerializedName("trust_level") val trustLevel: Int = 0
) {
    fun getAvatarUrl(baseUrl: String, size: Int = 180): String {
        val cleanBase = baseUrl.trimEnd('/')
        val template = if (avatarTemplate.startsWith("http://") || avatarTemplate.startsWith("https://")) {
            avatarTemplate
        } else {
            "$cleanBase$avatarTemplate"
        }
        return template.replace("{size}", size.toString())
    }
}

// Model for Discourse Image Album Plugin
data class AlbumListResponse(
    @SerializedName("albums") val albums: List<Album> = emptyList()
)

data class Album(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("items_count") val itemsCount: Int = 0,
    @SerializedName("cover_image_url") val coverImageUrl: String? = null,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
)

data class AlbumItemListResponse(
    @SerializedName("items") val items: List<AlbumItem> = emptyList()
)

data class AlbumItem(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("album_id") val albumId: Long = 0,
    @SerializedName("url") val url: String = "",
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("caption") val caption: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)

data class CsrfResponse(
    @SerializedName("csrf") val csrf: String = ""
)

data class SessionLoginResponse(
    @SerializedName("user") val user: User? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errors") val errors: List<String>? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("second_factor_required") val secondFactorRequired: Boolean = false,
    @SerializedName("backup_codes_enabled") val backupCodesEnabled: Boolean = false
)

data class UploadResponse(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("url") val url: String = "",
    @SerializedName("original_filename") val originalFilename: String = "",
    @SerializedName("filesize") val filesize: Long = 0,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("short_url") val shortUrl: String? = null,
    @SerializedName("short_path") val shortPath: String? = null,
    @SerializedName("extension") val extension: String? = null
)

data class PrepareVideoUploadResponse(
    @SerializedName("upload_url") val uploadUrl: String? = null,
    @SerializedName("video_id") val videoId: String? = null,
    @SerializedName("stream_media_id") val streamMediaId: String? = null
)

data class CreatePostRequest(
    @SerializedName("raw") val raw: String,
    @SerializedName("topic_id") val topicId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("category") val categoryId: Long? = null,
    @SerializedName("reply_to_post_number") val replyToPostNumber: Int? = null,
    @SerializedName("target_recipients") val targetRecipients: String? = null,
    @SerializedName("archetype") val archetype: String? = null
)

data class CreatePostResponse(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("topic_id") val topicId: Long = 0,
    @SerializedName("post_number") val postNumber: Int = 1
)

data class PostActionRequest(
    @SerializedName("id") val postId: Long,
    @SerializedName("post_action_type_id") val postActionTypeId: Int = 2 // 2 = Like
)
