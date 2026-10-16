-- Store product lifecycle as numeric codes:
-- 1 = ACTIVE, 2 = DELETED (soft deleted).

-- Remove old enum/check constraints before changing the column type.
DO $$
DECLARE
    status_constraint RECORD;
BEGIN
    FOR status_constraint IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'products'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) ILIKE '%status%'
    LOOP
        EXECUTE FORMAT(
            'ALTER TABLE products DROP CONSTRAINT %I',
            status_constraint.conname
        );
    END LOOP;
END
$$;

ALTER TABLE products
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE products
    ALTER COLUMN status TYPE SMALLINT
    USING (
        CASE
            WHEN status::TEXT IN ('ACTIVE', '1') THEN 1
            ELSE 2
        END
    );

UPDATE products
SET status = 1
WHERE status IS NULL;

ALTER TABLE products
    ALTER COLUMN status SET DEFAULT 1,
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS chk_products_status;

ALTER TABLE products
    ADD CONSTRAINT chk_products_status CHECK (status IN (1, 2));

CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
