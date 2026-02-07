

def get_db_url(driver: str,
               port: int,
               username: str,
               password: str,
               host: str,
               database: str, ):
    match driver:
        case "sqlite":
            return f"sqlite:///{database}"
        case "aiosqlite":
            return f"sqlite+aiosqlite:///{database}"
        case "mysql":
            return f"mysql+pymysql://{username}:{password}@{host}:{port}/{database}"
        case "postgresql":
            return f"postgresql://{username}:{password}@{host}:{port}/{database}"
        case "oracle":
            return f"oracle://{username}:{password}@{host}:{port}/{database}"
        case _:
            return f"sqlite:///{database}"
