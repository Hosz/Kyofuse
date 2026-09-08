require('dotenv').config()
const { Pool } = require("pg")

const DbConfig = {
    user: process.env.DB_USER || 'kyofuse',
    host: process.env.DB_HOST || 'localhost',
    database: process.env.DB_DATABASE || 'kyofuse',
    password: process.env.DB_PASSWORD || 'change-me',
    port: process.env.DB_PORT || 5432
}

export async function executeSQL(sqlScript) {
    try {
        const pool = new Pool(DbConfig)
        const client = await pool.connect()

        const result = await client.query(sqlScript)
        await client.release()
        await pool.end()
        return result.rows
    } catch (error) {
        console.log('Erro ao executar o script SQL: ' + error)
    }
}

export const userLogin = {
    username: 'testeee',
    password: 'senhateste123'
}

export const userPending = {
    username: 'usuariopendente',
    password: 'senhateste123'
}

export const userRegister = {
    name: 'usuario',
    lastName: 'teste',
    email: 'usuarioteste@agencia.com',
    username: 'usuarioteste',
    password: 'senhateste123'
}

export const invalidUsers = {
    invalidEmail: {
        name: 'usuario',
        lastName: 'teste',
        email: 'usuarioteste-invalido',
        username: 'usuarioteste',
        password: 'senhateste123',
        confirmPassword: 'senhateste123'
    },
    passwordMismatch: {
        name: 'usuario',
        lastName: 'teste',
        email: 'usuarioteste@agencia.com',
        username: 'usuarioteste',
        password: 'senhateste123',
        confirmPassword: 'senhadiferente'
    },
    shortPassword: {
        name: 'usuario',
        lastName: 'teste',
        email: 'usuarioteste@agencia.com',
        username: 'usuarioteste',
        password: '123',
        confirmPassword: '123'
    }
}

export const profileData = {
    nickname: 'ProPlayer_Kyofuse',
    bio: 'Jogador focado em CS2 competitivo e busca de time.'
}

export const feedData = {
    postContent: 'Treino tático de CS2 hoje às 20h! Quem anima?'
}
