package Server.Database;

public class ChangeEntity {
    private SQLCreator sqlCreator;
    private BaseEntity entity;
    private boolean isNew;

    public boolean isNew() {
        return isNew;
    }

    public ChangeEntity(BaseEntity entity, SQLCreator sqlCreator, boolean isNew) {
        super();
        this.entity = entity;
        this.sqlCreator = sqlCreator;
        this.isNew = isNew;

    }

    public SQLCreator getSqlCreator() {
        return sqlCreator;
    }

    public void setSqlCreator(SQLCreator sqlCreator) {
        this.sqlCreator = sqlCreator;
    }

    public BaseEntity getEntity() {
        return entity;
    }

    public void setEntity(BaseEntity entity) {
        this.entity = entity;
    }
}
