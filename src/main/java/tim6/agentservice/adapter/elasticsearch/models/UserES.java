package tim6.agentservice.adapter.elasticsearch.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import tim6.agentservice.domain.model.User;


@Document(indexName = "user_preferences")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserES {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String sex;

    @Field(type = FieldType.Integer)
    private Integer birthYear;

    @Field(type = FieldType.Keyword)
    private List<String> postedTagsHistory;

    public static UserES from(User user) {
        return new UserES(user.getId(), user.getSex(), user.getBirthYear(),
                user.getPostedTagsHistory());
    }

    public static User to(UserES userES) {
        return new User(userES.getId(), userES.getSex(), userES.getBirthYear(),
                userES.getPostedTagsHistory());
    }

}
