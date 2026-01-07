-- Add soft delete column to product table
ALTER TABLE product ADD COLUMN deleted_at TIMESTAMP;

-- Create index on deleted_at for better query performance
CREATE INDEX idx_product_deleted_at ON product(deleted_at);

-- Add compound index for active + deleted queries (common query pattern)
CREATE INDEX idx_product_active_not_deleted ON product(is_active, deleted_at);
