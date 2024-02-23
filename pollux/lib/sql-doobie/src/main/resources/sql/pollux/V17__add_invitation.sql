
-- Introduce new field invitation  for connection less presentation
ALTER TABLE public.presentation_records
    ADD COLUMN "invitation" TEXT;