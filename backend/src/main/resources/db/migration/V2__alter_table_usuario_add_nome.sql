-- 1. Adiciona a coluna permitindo nulo temporariamente
ALTER TABLE Usuario ADD COLUMN nome VARCHAR(255);

-- 2. Atualiza os usuários antigos (caso existam) com um nome padrão para não quebrar o banco
UPDATE Usuario SET nome = 'Usuário Padrão' WHERE nome IS NULL;

-- 3. Agora sim, trava a coluna para ser NOT NULL e respeitar a sua classe Java
ALTER TABLE Usuario MODIFY COLUMN nome VARCHAR(255) NOT NULL;