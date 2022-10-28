package cc.towerdefence.api.friendmanager.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "pendingFriendConnection")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingFriendConnection {

    @Id
    private ObjectId id;

    @Indexed
    private UUID requesterId;

    @Indexed
    private UUID targetId;
}
