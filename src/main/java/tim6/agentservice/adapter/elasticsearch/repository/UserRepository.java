package tim6.agentservice.adapter.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import tim6.agentservice.adapter.elasticsearch.models.UserES;

@Repository
public interface UserRepository extends ElasticsearchRepository<UserES, String> {

}
