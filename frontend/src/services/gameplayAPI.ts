// Starts quizzes and submits guesses to the backend.
const BASE = '/api/quizzes';

export interface StartQuizResponse {
  sessionId: string;
  mapId: string;
  currentTarget: string;
}

export interface GuessResponse {
  correct: boolean;
  correctionRequired: boolean;
  selectedRegionId: string;
  targetRegionId: string;
  currentTargetRegionId: string;
  correctAnswers: number;
  answeredTargets: number;
  accuracy: number;
  quizComplete: boolean;
}

async function post<T>(
  path: string,
  body: unknown
): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw new Error(
      `Gameplay request failed: ${response.status}`
    );
  }

  return response.json() as Promise<T>;
}

export function startQuiz(
  mapId: string,
  userId: number | null
): Promise<StartQuizResponse> {
  return post<StartQuizResponse>('', {
    mapId,
    userId,
  });
}

export function submitGuess(
  sessionId: string,
  selectedRegionId: string,
  clickedLat?: number,
  clickedLng?: number
): Promise<GuessResponse> {
  return post<GuessResponse>(
    `/${sessionId}/guesses`,
    {
      selectedRegionId,
      clickedLat,
      clickedLng,
    }
  );
}