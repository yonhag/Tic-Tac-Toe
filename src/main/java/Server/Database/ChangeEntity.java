package Server.Database;

public record ChangeEntity(BaseEntity entity, SQLCreator sqlCreator, boolean isNew) {
}
