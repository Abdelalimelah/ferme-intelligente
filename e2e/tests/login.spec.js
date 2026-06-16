import { test, expect } from '@playwright/test';

// Exercises the full stack through nginx: frontend → backend → Postgres,
// using the seeded demo accounts from the V1 Flyway migration.

test('manager can log in and reach the dashboard', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Email').fill('karim@ferme.ma');
  await page.getByLabel('Mot de passe').fill('password123');
  await page.getByRole('button', { name: 'Se connecter' }).click();

  await expect(page).toHaveURL(/\/manager/);
  await expect(page.getByRole('heading', { name: 'Dashboard Gestionnaire' })).toBeVisible();
});

test('wrong password is rejected with an error message', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Email').fill('karim@ferme.ma');
  await page.getByLabel('Mot de passe').fill('wrong-password');
  await page.getByRole('button', { name: 'Se connecter' }).click();

  await expect(page).toHaveURL(/\/login/);
  await expect(page.locator('text=/erreur|incorrect/i')).toBeVisible();
});

test('demo quick-access button logs in as the owner', async ({ page }) => {
  await page.goto('/login');

  await page.getByRole('button', { name: 'Propriétaire' }).click();

  await expect(page).toHaveURL(/\/owner/);
});
