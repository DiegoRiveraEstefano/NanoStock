import os
from cryptography.fernet import Fernet
import bcrypt


class CryptoUtils:
    """
    Utilidades para manejo de contraseñas y cifrado de datos.
    """

    # --- HASHING DE CONTRASEÑAS (BCRYPT) ---

    @staticmethod
    def hash_password(password: str) -> str:
        """Genera un hash seguro para una contraseña."""
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
        return hashed.decode('utf-8')

    @staticmethod
    def verify_password(password: str, hashed_password: str) -> bool:
        """Verifica si una contraseña coincide con su hash."""
        return bcrypt.checkpw(
            password.encode('utf-8'),
            hashed_password.encode('utf-8')
        )

    # --- CIFRADO SIMÉTRICO (FERNET / AES) ---

    @staticmethod
    def generate_key() -> str:
        """Genera una llave maestra para cifrado simétrico."""
        return Fernet.generate_key().decode('utf-8')

    @staticmethod
    def encrypt_data(data: str, secret_key: str) -> str:
        """Cifra un texto plano usando una llave maestra."""
        f = Fernet(secret_key.encode('utf-8'))
        encrypted = f.encrypt(data.encode('utf-8'))
        return encrypted.decode('utf-8')

    @staticmethod
    def decrypt_data(encrypted_data: str, secret_key: str) -> str:
        """Descifra un texto cifrado."""
        f = Fernet(secret_key.encode('utf-8'))
        decrypted = f.decrypt(encrypted_data.encode('utf-8'))
        return decrypted.decode('utf-8')

    # --- GENERACIÓN DE TOKENS SEGUROS ---

    @staticmethod
    def generate_random_token(bytes_length: int = 32) -> str:
        """Genera un token aleatorio seguro (para URLs, salts, etc)."""
        return os.urandom(bytes_length).hex()
