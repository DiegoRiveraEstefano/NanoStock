from typing import Literal

from pydantic_settings import BaseSettings

from src.core.utils.db_url import get_db_url


class Settings(BaseSettings):

    app_name: str = "NanoStock"
    app_version: str = "1.0.0"
    app_env: Literal["dev", "test", "prod"] = "dev"

    db_driver: str = "aiosqlite"
    db_database: str = "nanostock"
    db_user: str = "nanostock"
    db_password: str = ""
    db_host: str = "localhost"
    db_port: int = 3306
    db_pool_size: int = 6
    db_max_overflow: int = 6
    db_pool_recycle: int = 60 * 60 * 24 * 7 #

    encryption_key: str = ""

    ttl_short: int = 5 # in minutes
    ttl_medium: int = 15
    ttl_long: int = 45

    pagination_page_limit: int = 10

    @property
    def is_prod(self) -> bool:
        return True if self.app_env == "prod" else False

    @property
    def get_db_url(self) -> str:
        return get_db_url(
            driver=self.db_driver,
            database=self.db_database,
            host=self.db_host,
            port=self.db_port,
            password=self.db_password,
            username=self.db_user,
        )

    class Config:
        env_file = ".env"


settings = Settings()
