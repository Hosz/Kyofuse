export async function getConfirmationLink(email) {
    // 1. Busca mensagens enviadas para esse e-mail no Mailpit
    const response = await fetch(`http://localhost:8025/api/v1/search?query=to:${email}`);
    const data = await response.json();

    if (!data.messages || data.messages.length === 0) {
        throw new Error(`Nenhum e-mail encontrado para: ${email}`);
    }

    // 2. Pega o corpo do e-mail mais recente
    const messageId = data.messages[0].ID;
    const msgRes = await fetch(`http://localhost:8025/api/v1/message/${messageId}`);
    const message = await msgRes.json();

    // 3. Extrai o link de confirmação do texto
    const match = message.Text.match(/http:\/\/localhost:4200\/verificar-email\?token=[^\s]+/);
    return match ? match[0] : null;
}
