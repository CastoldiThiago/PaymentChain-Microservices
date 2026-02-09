import { useEffect, useState, useCallback } from 'react';
import { generateIdempotencyKey } from '../utils/idempotency';

interface UseIdempotencyKeyResult {
  key: string | null;
  regenerate: () => void;
}

export const useIdempotencyKey = (): UseIdempotencyKeyResult => {
  const [key, setKey] = useState<string | null>(null);

  useEffect(() => {
    setKey(generateIdempotencyKey());
  }, []);

  const regenerate = useCallback(() => {
    setKey(generateIdempotencyKey());
  }, []);

  return { key, regenerate };
};
